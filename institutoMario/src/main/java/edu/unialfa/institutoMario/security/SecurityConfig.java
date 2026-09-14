package edu.unialfa.institutoMario.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Autowired
    private SecurityFilter securityFilter;

    @Autowired
    private AuditoriaLoginHandlers auditoriaLoginHandlers;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(apiAuthenticationEntryPoint))
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }


    // O módulo Benefícios lida com dado sensível (CPF, renda, endereço) via
    // formulário HTML tradicional (nunca API/JSON — ver CLAUDE.md do módulo),
    // então precisa de CSRF LIGADO, diferente do resto da aplicação web
    // (webSecurityChain, abaixo, desliga CSRF por decisão pré-existente do
    // projeto). Por isso ganha sua própria chain, escopada só a
    // "/beneficios/**", em vez de mexer no comportamento global.
    @Bean
    @Order(2)
    public SecurityFilterChain beneficiosSecurityChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .securityMatcher("/beneficios/**")
                .cors(Customizer.withDefaults())
                .headers(header -> header.frameOptions(config -> config.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasRole("ADM")
                )
                .formLogin(login -> login.loginPage("/login"))
                .build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain webSecurityChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .headers(header -> header.frameOptions(config -> config.sameOrigin()))
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/login").permitAll()
                        .requestMatchers("/esqueci-senha", "/recuperar-senha").permitAll()
                        .requestMatchers("/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/turmas/**").hasRole("ADMIN")
                        .requestMatchers("/relatorios/**").hasRole("ADMIN")
                        .requestMatchers("/vinculo-aluno-turma/**").hasRole("ADMIN")
                        .requestMatchers("/projetos/**").hasRole("ADMIN")
                        .requestMatchers("/logs-auditoria/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .successHandler(auditoriaLoginHandlers.successHandler())
                        .failureHandler(auditoriaLoginHandlers.failureHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(auditoriaLoginHandlers.logoutSuccessHandler())
                )
                .build();
    }
}