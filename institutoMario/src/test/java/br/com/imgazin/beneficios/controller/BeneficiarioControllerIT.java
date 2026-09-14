package br.com.imgazin.beneficios.controller;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de ponta a ponta do fluxo de cadastro (ver CLAUDE.md do módulo,
 * Etapa 2). Sobe o contexto completo (mesma razão do
 * {@code BeneficiosHomeSecurityIT} da Etapa 1: a SecurityConfig não dá pra
 * fatiar sem mockar meio sistema legado) com Testcontainers + ddl-auto=update
 * só para o teste (banco fresco: Flyway cria as tabelas de Benefícios,
 * Hibernate cria as legadas).
 * <p>
 * CPFs usados nos testes têm dígito verificador conferido manualmente (ver
 * comentário em cada método) — nenhum é aleatório.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class BeneficiarioControllerIT {

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
    private BeneficiarioRepository beneficiarioRepository;

    private RequestPostProcessor comoAdmin() {
        return SecurityMockMvcRequestPostProcessors.user("admin").roles("ADM");
    }

    /** CPF 123.456.789-09 — dígito verificador válido, sem nenhum outro dado que dispare aviso. */
    @Test
    void cadastroSimplesRedirecionaComFlashDeSucesso() throws Exception {
        mockMvc.perform(post("/beneficios/beneficiarios/salvar")
                        .with(comoAdmin())
                        .with(csrf())
                        .param("cpf", "123.456.789-09")
                        .param("nome", "Maria Cadastro Simples Teste")
                        .param("status", "AGUARDANDO_ANALISE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("sucesso"));

        assertThat(beneficiarioRepository.findByCpf("12345678909")).isPresent();
    }

    /**
     * CPF 111.444.777-30 — mesma base do CPF de teste "111.444.777-35" (válido,
     * documentado em CpfUtilsTest), com o último dígito trocado para ficar
     * inválido. Usado com /confirmar direto (precisa confirmar o aviso ALTA
     * de dígito inválido) para existir um primeiro cadastro; a segunda
     * tentativa com o MESMO CPF deve ser barrada por duplicidade — o único
     * caso de negócio que realmente bloqueia.
     */
    @Test
    void cpfDuplicadoReexibeFormularioComErroNoCampo() throws Exception {
        mockMvc.perform(post("/beneficios/beneficiarios/confirmar")
                        .with(comoAdmin())
                        .with(csrf())
                        .param("cpf", "111.444.777-30")
                        .param("nome", "Primeiro Cadastro Duplicidade Teste")
                        .param("status", "AGUARDANDO_ANALISE")
                        .param("avisosConfirmados", "CPF_DIGITO_INVALIDO")
                        .param("justificativa", "Documento físico conferido presencialmente."))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/beneficios/beneficiarios/salvar")
                        .with(comoAdmin())
                        .with(csrf())
                        .param("cpf", "111.444.777-30")
                        .param("nome", "Segundo Cadastro Duplicidade Teste")
                        .param("status", "AGUARDANDO_ANALISE"))
                .andExpect(status().isOk())
                .andExpect(view().name("beneficios/beneficiarios/formulario"))
                .andExpect(model().attributeHasFieldErrors("beneficiarioForm", "cpf"));
    }

    /** CPF 123.456.789-01 — mesma base válida "123.456.789-09", segundo dígito verificador trocado (inválido). */
    @Test
    void avisoAltaNaoConfirmadoMostraTelaDeConfirmacao() throws Exception {
        mockMvc.perform(post("/beneficios/beneficiarios/salvar")
                        .with(comoAdmin())
                        .with(csrf())
                        .param("cpf", "123.456.789-01")
                        .param("nome", "Pessoa Cpf Invalido Teste")
                        .param("status", "AGUARDANDO_ANALISE"))
                .andExpect(status().isOk())
                .andExpect(view().name("beneficios/beneficiarios/confirmacao"))
                .andExpect(model().attribute("exigeJustificativa", true));
    }

    /** CPF 111.444.777-45 — mesma base válida, primeiro dígito verificador trocado (inválido). */
    @Test
    void confirmacaoComAvisoENotaGravaERedireciona() throws Exception {
        mockMvc.perform(post("/beneficios/beneficiarios/confirmar")
                        .with(comoAdmin())
                        .with(csrf())
                        .param("cpf", "111.444.777-45")
                        .param("nome", "Pessoa Confirmada Teste")
                        .param("status", "AGUARDANDO_ANALISE")
                        .param("avisosConfirmados", "CPF_DIGITO_INVALIDO")
                        .param("justificativa", "Documento físico conferido presencialmente."))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("sucesso"));

        Beneficiario salvo = beneficiarioRepository.findByCpf("11144477745").orElseThrow();
        assertThat(salvo.getNome()).isEqualTo("Pessoa Confirmada Teste");
    }
}
