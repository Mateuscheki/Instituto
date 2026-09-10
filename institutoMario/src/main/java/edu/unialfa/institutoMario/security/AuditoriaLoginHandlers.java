package edu.unialfa.institutoMario.security;

import edu.unialfa.institutoMario.audit.LogAuditoriaService;
import edu.unialfa.institutoMario.audit.TipoAcao;
import edu.unialfa.institutoMario.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Registra no log de auditoria os eventos de login/logout da parte web (sessão via
 * formulário), delegando o redirecionamento de fato para o comportamento padrão do
 * Spring Security que a aplicação já usava.
 */
@Component
@RequiredArgsConstructor
public class AuditoriaLoginHandlers {

    private final LogAuditoriaService logAuditoriaService;

    public AuthenticationSuccessHandler successHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            if (authentication.getPrincipal() instanceof Usuario usuario) {
                logAuditoriaService.registrar(usuario, TipoAcao.LOGIN_SUCESSO, "Usuario", usuario.getId(),
                        "Login via sistema web realizado com sucesso");
            }
            response.sendRedirect(request.getContextPath() + "/");
        };
    }

    public AuthenticationFailureHandler failureHandler() {
        AuthenticationFailureHandler padrao = new SimpleUrlAuthenticationFailureHandler("/login?error");
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) -> {
            String tentativa = request.getParameter("username");
            logAuditoriaService.registrarLoginFalha(tentativa, "Login via sistema web negado: credenciais inválidas");
            padrao.onAuthenticationFailure(request, response, exception);
        };
    }

    public LogoutSuccessHandler logoutSuccessHandler() {
        SimpleUrlLogoutSuccessHandler padrao = new SimpleUrlLogoutSuccessHandler();
        padrao.setDefaultTargetUrl("/login");
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
                logAuditoriaService.registrar(usuario, TipoAcao.LOGOUT, "Usuario", usuario.getId(),
                        "Logout realizado");
            }
            padrao.onLogoutSuccess(request, response, authentication);
        };
    }
}
