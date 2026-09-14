package br.com.imgazin.beneficios.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Sobe o contexto Spring inteiro (não dá para "fatiar" a SecurityConfig sem
 * mockar meio sistema legado — ela também monta a chain de /api/** e a chain
 * web principal, ambas usadas pelo resto da aplicação). Por isso usa
 * Testcontainers com {@code ddl-auto=update} só neste teste: banco fresco,
 * o Flyway cria as tabelas de Benefícios e o Hibernate cria as ~20 tabelas
 * legadas (que não têm migration) — em produção, o banco já existe e
 * ddl-auto continua "update" também (ver nota em application.properties).
 * <p>
 * A autenticação usa {@code user(...).roles("ADM")} do spring-security-test
 * — não depende do login real (Usuario/CPF/senha) para testar só a regra de
 * autorização do caminho /beneficios/**.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class BeneficiosHomeSecurityIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Banco fresco (Testcontainers): sem isso, ddl-auto=validate/baseline
        // do application.properties quebraria por não achar as ~20 tabelas
        // legadas, que não têm migration Flyway (só a fundação de Benefícios tem).
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void usuarioSemRoleAdmRecebe403() throws Exception {
        mockMvc.perform(get("/beneficios").with(user("professor").roles("PROFESSOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioComRoleAdmRecebeStatusOkEAViewHome() throws Exception {
        mockMvc.perform(get("/beneficios").with(user("admin").roles("ADM")))
                .andExpect(status().isOk())
                .andExpect(view().name("beneficios/home"));
    }

    @Test
    void usuarioNaoAutenticadoERedirecionadoParaLogin() throws Exception {
        mockMvc.perform(get("/beneficios"))
                .andExpect(status().is3xxRedirection());
    }
}
