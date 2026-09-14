package br.com.imgazin.beneficios.thymeleaf;

import br.com.imgazin.beneficios.util.CpfUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Objeto exposto nas views do módulo como {@code #cpf} (ver {@link CpfDialect}).
 * Uso: {@code th:text="${#cpf.mascararConformePerfil(beneficiario.cpf)}"}.
 */
public class CpfExpressionUtility {

    public String formatar(String cpf) {
        return CpfUtils.formatar(cpf);
    }

    public String mascarar(String cpf) {
        return CpfUtils.mascarar(cpf);
    }

    /**
     * CPF completo formatado para quem tem ROLE_ADM (dado sensível liberado
     * por perfil — ver CLAUDE.md/LGPD); mascarado para qualquer outro caso,
     * inclusive sem autenticação.
     */
    public String mascararConformePerfil(String cpf) {
        if (temRoleAdm()) {
            return CpfUtils.formatar(cpf);
        }
        return CpfUtils.mascarar(cpf);
    }

    private boolean temRoleAdm() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADM"));
    }
}
