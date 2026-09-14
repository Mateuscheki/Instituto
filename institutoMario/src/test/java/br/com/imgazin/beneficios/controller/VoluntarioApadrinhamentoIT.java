package br.com.imgazin.beneficios.controller;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.RespostaSimNao;
import br.com.imgazin.beneficios.domain.RetiradaCesta;
import br.com.imgazin.beneficios.domain.StatusBeneficiario;
import br.com.imgazin.beneficios.domain.TipoRetirante;
import br.com.imgazin.beneficios.domain.Voluntario;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import br.com.imgazin.beneficios.repository.RetiradaCestaRepository;
import br.com.imgazin.beneficios.repository.VoluntarioRepository;
import br.com.imgazin.beneficios.service.ApadrinhamentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Mesmo padrão de infraestrutura de teste das etapas anteriores (contexto
 * completo + Testcontainers + ddl-auto=update só no teste — ver
 * BeneficiosHomeSecurityIT/BeneficiarioControllerIT).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class VoluntarioApadrinhamentoIT {

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
    private MockMvc mockMvc;
    @Autowired
    private VoluntarioRepository voluntarioRepository;
    @Autowired
    private BeneficiarioRepository beneficiarioRepository;
    @Autowired
    private RetiradaCestaRepository retiradaCestaRepository;
    @Autowired
    private ApadrinhamentoService apadrinhamentoService;

    private RequestPostProcessor comoAdmin() {
        return SecurityMockMvcRequestPostProcessors.user("admin").roles("ADM");
    }

    @Test
    void cadastroSimplesDeVoluntarioRedirecionaComSucesso() throws Exception {
        mockMvc.perform(post("/beneficios/voluntarios/salvar").with(comoAdmin()).with(csrf())
                        .param("nome", "Voluntário Teste Simples")
                        .param("areaAtuacao", "Entrega"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("sucesso"));
    }

    @Test
    void cpfDuplicadoDeVoluntarioReexibeFormularioComErro() throws Exception {
        mockMvc.perform(post("/beneficios/voluntarios/salvar").with(comoAdmin()).with(csrf())
                .param("nome", "Primeiro Voluntário Cpf")
                .param("cpf", "111.444.777-35"));

        mockMvc.perform(post("/beneficios/voluntarios/salvar").with(comoAdmin()).with(csrf())
                        .param("nome", "Segundo Voluntário Cpf")
                        .param("cpf", "111.444.777-35"))
                .andExpect(status().isOk())
                .andExpect(view().name("voluntarios/formulario"))
                .andExpect(model().attributeHasFieldErrors("voluntarioForm", "cpf"));
    }

    @Test
    void vincularPadrinhoJaAtivoNaoExigeConfirmacao() throws Exception {
        Voluntario padrinho = new Voluntario();
        padrinho.setNome("Padrinho Já Ativo");
        padrinho.setPadrinho(true);
        padrinho.setAtivo(true);
        padrinho = voluntarioRepository.save(padrinho);

        Beneficiario beneficiario = criarBeneficiario("Beneficiário Vinculo Direto", "11144477735");

        mockMvc.perform(post("/beneficios/beneficiarios/{id}/padrinho", beneficiario.getId())
                        .with(comoAdmin()).with(csrf())
                        .param("voluntarioId", padrinho.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("sucesso"));

        Beneficiario atualizado = beneficiarioRepository.findById(beneficiario.getId()).orElseThrow();
        assertThat(atualizado.getPadrinho().getId()).isEqualTo(padrinho.getId());
    }

    @Test
    void vincularVoluntarioQueNaoEhPadrinhoExigeConfirmacaoEMarcaAFlag() throws Exception {
        Voluntario voluntarioComum = new Voluntario();
        voluntarioComum.setNome("Voluntário Não Padrinho");
        voluntarioComum.setPadrinho(false);
        voluntarioComum.setAtivo(true);
        voluntarioComum = voluntarioRepository.save(voluntarioComum);

        Beneficiario beneficiario = criarBeneficiario("Beneficiário Confirmacao Padrinho", "12345678909");

        // Sem confirmar: volta a tela de confirmação, não vincula ainda
        mockMvc.perform(post("/beneficios/beneficiarios/{id}/padrinho", beneficiario.getId())
                        .with(comoAdmin()).with(csrf())
                        .param("voluntarioId", voluntarioComum.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("beneficios/beneficiarios/confirmar-padrinho"));

        assertThat(beneficiarioRepository.findById(beneficiario.getId()).orElseThrow().getPadrinho()).isNull();

        // Confirmando + justificativa: vincula e marca a flag is_padrinho
        mockMvc.perform(post("/beneficios/beneficiarios/{id}/padrinho", beneficiario.getId())
                        .with(comoAdmin()).with(csrf())
                        .param("voluntarioId", voluntarioComum.getId().toString())
                        .param("avisosConfirmados", "VOLUNTARIO_NAO_E_PADRINHO")
                        .param("justificativa", "Aprovado pela coordenação."))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("sucesso"));

        Voluntario recarregado = voluntarioRepository.findById(voluntarioComum.getId()).orElseThrow();
        assertThat(recarregado.isPadrinho()).isTrue();
        Beneficiario beneficiarioAtualizado = beneficiarioRepository.findById(beneficiario.getId()).orElseThrow();
        assertThat(beneficiarioAtualizado.getPadrinho().getId()).isEqualTo(voluntarioComum.getId());
    }

    @Test
    void trocarPadrinhoNaoAlteraORetiranteDeUmaRetiradaJaRegistrada() {
        Voluntario padrinhoAntigo = new Voluntario();
        padrinhoAntigo.setNome("Padrinho Antigo");
        padrinhoAntigo.setPadrinho(true);
        padrinhoAntigo.setAtivo(true);
        padrinhoAntigo = voluntarioRepository.save(padrinhoAntigo);

        Voluntario padrinhoNovo = new Voluntario();
        padrinhoNovo.setNome("Padrinho Novo");
        padrinhoNovo.setPadrinho(true);
        padrinhoNovo.setAtivo(true);
        padrinhoNovo = voluntarioRepository.save(padrinhoNovo);

        Beneficiario beneficiario = criarBeneficiario("Beneficiário Troca Padrinho", "22233344405");

        apadrinhamentoService.vincular(beneficiario.getId(), padrinhoAntigo.getId(), List.of(), null, "teste");

        RetiradaCesta retirada = new RetiradaCesta();
        retirada.setBeneficiario(beneficiarioRepository.findById(beneficiario.getId()).orElseThrow());
        retirada.setMesReferencia(LocalDate.now().withDayOfMonth(1));
        retirada.setDataRetirada(LocalDateTime.now());
        retirada.setQuantidadeCestas(1);
        retirada.setTipoRetirante(TipoRetirante.PADRINHO);
        retirada.setRetiranteNome(padrinhoAntigo.getNome());
        retirada.setRetiradoPorPadrinho(true);
        retirada.setPadrinho(padrinhoAntigo);
        retirada.setRegistradoPor("teste");
        retirada = retiradaCestaRepository.save(retirada);

        // Troca o padrinho vinculado ao beneficiário
        apadrinhamentoService.vincular(beneficiario.getId(), padrinhoNovo.getId(), List.of(), null, "teste");

        Beneficiario atualizado = beneficiarioRepository.findById(beneficiario.getId()).orElseThrow();
        assertThat(atualizado.getPadrinho().getId()).isEqualTo(padrinhoNovo.getId());

        // A retirada já registrada continua apontando para o padrinho de quando foi feita
        RetiradaCesta retiradaRecarregada = retiradaCestaRepository.findById(retirada.getId()).orElseThrow();
        assertThat(retiradaRecarregada.getPadrinho().getId()).isEqualTo(padrinhoAntigo.getId());
    }

    @Test
    void telaDeApadrinhadosMostraQuemJaRetirouNoMes() throws Exception {
        Voluntario padrinho = new Voluntario();
        padrinho.setNome("Padrinho Tela Apadrinhados");
        padrinho.setPadrinho(true);
        padrinho.setAtivo(true);
        padrinho = voluntarioRepository.save(padrinho);

        Beneficiario retirou = criarBeneficiario("Apadrinhado Que Retirou", "33344455508");
        Beneficiario naoRetirou = criarBeneficiario("Apadrinhado Que Nao Retirou", "44455566619");

        apadrinhamentoService.vincular(retirou.getId(), padrinho.getId(), List.of(), null, "teste");
        apadrinhamentoService.vincular(naoRetirou.getId(), padrinho.getId(), List.of(), null, "teste");

        RetiradaCesta retirada = new RetiradaCesta();
        retirada.setBeneficiario(beneficiarioRepository.findById(retirou.getId()).orElseThrow());
        retirada.setMesReferencia(LocalDate.now().withDayOfMonth(1));
        retirada.setDataRetirada(LocalDateTime.now());
        retirada.setQuantidadeCestas(1);
        retirada.setTipoRetirante(TipoRetirante.PROPRIO);
        retirada.setRetiranteNome(retirou.getNome());
        retirada.setRegistradoPor("teste");
        retiradaCestaRepository.save(retirada);

        mockMvc.perform(get("/beneficios/padrinhos/{voluntarioId}/apadrinhados", padrinho.getId()).with(comoAdmin()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("apadrinhados", org.hamcrest.Matchers.hasSize(2)));
    }

    private Beneficiario criarBeneficiario(String nome, String cpf) {
        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setNome(nome);
        beneficiario.setCpf(cpf);
        beneficiario.setStatus(StatusBeneficiario.AGUARDANDO_ANALISE);
        beneficiario.setAtivo(true);
        beneficiario.setTemFilhos(RespostaSimNao.NAO_INFORMADO);
        return beneficiarioRepository.save(beneficiario);
    }
}
