package br.com.imgazin.beneficios.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita o {@code @Scheduled} usado pelo job de virada de mês do módulo. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
