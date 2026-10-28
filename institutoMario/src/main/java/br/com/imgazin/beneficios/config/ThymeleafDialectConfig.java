package br.com.imgazin.beneficios.config;

import br.com.imgazin.beneficios.thymeleaf.CpfDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.dialect.IDialect;

@Configuration
public class ThymeleafDialectConfig {

    @Bean
    public IDialect cpfDialect() {
        return new CpfDialect();
    }
}
