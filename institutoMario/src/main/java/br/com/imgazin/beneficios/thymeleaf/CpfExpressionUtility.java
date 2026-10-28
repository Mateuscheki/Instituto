package br.com.imgazin.beneficios.thymeleaf;

import br.com.imgazin.beneficios.util.CpfUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class CpfExpressionUtility {

    public String formatar(String cpf) {
        return CpfUtils.formatar(cpf);
    }

    public String mascarar(String cpf) {
        return CpfUtils.mascarar(cpf);
    }

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
