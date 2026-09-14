package br.com.imgazin.beneficios.config;

import edu.unialfa.institutoMario.model.Usuario;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Diz ao Spring Data JPA Auditing quem é o "autor" da gravação atual, para
 * preencher {@code criado_por}/{@code atualizado_por}. O login da aplicação
 * como um todo é o {@code edu.unialfa.institutoMario.model.Usuario} (ver
 * {@code SecurityFilter}); aqui só lemos o nome dele a partir do
 * SecurityContext — nenhuma lógica de autenticação nova.
 */
@Component("auditorAwareImpl")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("sistema");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Usuario usuario) {
            return Optional.ofNullable(usuario.getNome()).or(() -> Optional.of(authentication.getName()));
        }

        return Optional.ofNullable(authentication.getName()).or(() -> Optional.of("sistema"));
    }
}
