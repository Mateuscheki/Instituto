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
