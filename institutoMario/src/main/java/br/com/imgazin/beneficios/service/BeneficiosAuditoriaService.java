package br.com.imgazin.beneficios.service;

import br.com.imgazin.beneficios.domain.AcaoAuditoria;
import br.com.imgazin.beneficios.domain.BeneficiosAuditoria;
import br.com.imgazin.beneficios.repository.BeneficiosAuditoriaRepository;
import edu.unialfa.institutoMario.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Trilha de auditoria própria do módulo (tabela {@code beneficios_auditoria}
 * — LGPD: toda consulta/gravação de dado sensível fica registrada). Mesmo
 * espírito do {@code LogAuditoriaService} legado, mas não reaproveitado
 * diretamente porque grava numa tabela e com colunas diferentes
 * (inclusive {@code dados_antes}/{@code dados_depois} e {@code perfil}).
 */
@Service
@RequiredArgsConstructor
public class BeneficiosAuditoriaService {

    private final BeneficiosAuditoriaRepository beneficiosAuditoriaRepository;

    public void registrar(AcaoAuditoria acao, String entidade, Long entidadeId, String dadosDepois) {
        BeneficiosAuditoria auditoria = new BeneficiosAuditoria();
        auditoria.setEntidade(entidade);
        auditoria.setEntidadeId(entidadeId);
        auditoria.setAcao(acao);
        auditoria.setUsuario(usuarioAtual());
        auditoria.setPerfil(perfilAtual());
        auditoria.setIp(ipAtual());
        auditoria.setDadosDepois(dadosDepois);
        auditoria.setDataHora(LocalDateTime.now());
        beneficiosAuditoriaRepository.save(auditoria);
    }

    private String usuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario.getNome();
        }
        return "sistema";
    }

    private String perfilAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(role -> role.startsWith("ROLE_"))
                .map(role -> role.substring("ROLE_".length()))
                .findFirst()
                .orElse(null);
    }

    private String ipAtual() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
