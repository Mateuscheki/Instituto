package br.com.imgazin.beneficios.service;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.RespostaSimNao;
import br.com.imgazin.beneficios.domain.RetiradaCesta;
import br.com.imgazin.beneficios.domain.StatusBeneficiario;
import br.com.imgazin.beneficios.domain.TipoRetirante;
import br.com.imgazin.beneficios.dto.DashboardDto;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import br.com.imgazin.beneficios.repository.RetiradaCestaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Critérios de aceite da Etapa 5:
 * "Retiradas canceladas não entram em nenhum total" e
 * "pendentes + retiradas do mês = total de aptos do mês".
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class RelatorioServiceIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    private RelatorioService relatorioService;
    @Autowired
    private BeneficiarioRepository beneficiarioRepository;
    @Autowired
    private RetiradaCestaRepository retiradaCestaRepository;

    @Test
    void pendentesMaisRetiradasDoMesIgualAptosDoMesECanceladaNaoEntraNoTotal() {
        YearMonth mesAtual = YearMonth.now();
        LocalDate mesData = mesAtual.atDay(1);

        // Apto que retirou este mês (conta em "retiradasDoMes")
        Beneficiario retirou = criarBeneficiario("Aptos Retirou Teste", "11144477735", StatusBeneficiario.RETIRADO_MES_ATUAL);
        RetiradaCesta valida = new RetiradaCesta();
        valida.setBeneficiario(retirou);
        valida.setMesReferencia(mesData);
        valida.setDataRetirada(LocalDateTime.now());
        valida.setQuantidadeCestas(2);
        valida.setTipoRetirante(TipoRetirante.PROPRIO);
        valida.setRetiranteNome(retirou.getNome());
        valida.setRegistradoPor("teste");
        retiradaCestaRepository.save(valida);

        // Apto que ainda não retirou (conta em "pendentes")
        criarBeneficiario("Aptos Pendente Teste", "12345678909", StatusBeneficiario.APTO_MES_ATUAL);

        // Beneficiário com retirada CANCELADA no mês — não deve contar em nada
        Beneficiario cancelou = criarBeneficiario("Aptos Cancelou Teste", "22233344405", StatusBeneficiario.APTO_MES_ATUAL);
        RetiradaCesta cancelada = new RetiradaCesta();
        cancelada.setBeneficiario(cancelou);
        cancelada.setMesReferencia(mesData);
        cancelada.setDataRetirada(LocalDateTime.now());
        cancelada.setQuantidadeCestas(5); // se contasse, inflaria "cestasEntregues"
        cancelada.setTipoRetirante(TipoRetirante.PROPRIO);
        cancelada.setRetiranteNome(cancelou.getNome());
        cancelada.setRegistradoPor("teste");
        cancelada.setCancelada(true);
        retiradaCestaRepository.save(cancelada);

        DashboardDto dashboard = relatorioService.dashboard(mesAtual);

        // "Pendente" e "Cancelou" continuam APTO_MES_ATUAL (a retirada cancelada não
        // muda status); só "Retirou" conta em retiradasDoMes.
        assertThat(dashboard.getPendentes()).isEqualTo(2L);
        assertThat(dashboard.getRetiradasDoMes()).isEqualTo(1L);
        assertThat(dashboard.getPendentes() + dashboard.getRetiradasDoMes()).isEqualTo(dashboard.getAptosNoMes());
        // A retirada cancelada tinha 5 cestas — se ela contasse, o total seria 7, não 2.
        assertThat(dashboard.getCestasEntregues()).isEqualTo(2L);
    }

    private Beneficiario criarBeneficiario(String nome, String cpf, StatusBeneficiario status) {
        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setNome(nome);
        beneficiario.setCpf(cpf);
        beneficiario.setStatus(status);
        beneficiario.setAtivo(true);
        beneficiario.setTemFilhos(RespostaSimNao.NAO_INFORMADO);
        return beneficiarioRepository.save(beneficiario);
    }
}
