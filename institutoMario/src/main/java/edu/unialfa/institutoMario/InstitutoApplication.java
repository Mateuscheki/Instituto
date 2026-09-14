package edu.unialfa.institutoMario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// O módulo Benefícios vive em br.com.imgazin.beneficios (pacote raiz próprio,
// pedido pelo CLAUDE.md do módulo), fora da árvore edu.unialfa.institutoMario
// que o @SpringBootApplication varre por padrão — por isso o scan explícito
// dos três pontos abaixo (componentes, entidades JPA e repositórios).
@SpringBootApplication
@ComponentScan(basePackages = {"edu.unialfa.institutoMario", "br.com.imgazin.beneficios"})
@EntityScan(basePackages = {"edu.unialfa.institutoMario.model", "edu.unialfa.institutoMario.audit", "br.com.imgazin.beneficios.domain"})
@EnableJpaRepositories(basePackages = {"edu.unialfa.institutoMario.repository", "edu.unialfa.institutoMario.audit", "br.com.imgazin.beneficios.repository"})
public class  InstitutoApplication {

	public static void main(String[] args) {
		SpringApplication.run(InstitutoApplication.class, args);
	}

}
