package edu.unialfa.institutoMario.audit;

import edu.unialfa.institutoMario.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogAuditoriaService {

    private final LogAuditoriaRepository logAuditoriaRepository;

    /** Registra uma ação usando o usuário autenticado no momento (via SecurityContext). */
    public void registrar(TipoAcao acao, String entidade, Long entidadeId, String descricao) {
        salvar(usuarioAutenticadoAtual(), acao, entidade, entidadeId, descricao);
    }

    /** Registra uma ação para um usuário específico (útil quando o SecurityContext ainda não reflete o login, ex.: logo após autenticar). */
    public void registrar(Usuario usuario, TipoAcao acao, String entidade, Long entidadeId, String descricao) {
        salvar(usuario, acao, entidade, entidadeId, descricao);
    }

    /** Registra uma tentativa de login que falhou, quando não existe usuário autenticado para associar. */
    public void registrarLoginFalha(String identificadorTentativa, String motivo) {
        LogAuditoria log = new LogAuditoria();
        log.setDataHora(LocalDateTime.now());
        log.setUsuarioId(null);
        log.setUsuarioNome(identificadorTentativa);
        log.setAcao(TipoAcao.LOGIN_FALHA);
        log.setEntidade("Usuario");
        log.setDescricao(motivo);
        log.setIp(obterIpRequisicaoAtual());
        logAuditoriaRepository.save(log);
    }

    private void salvar(Usuario usuario, TipoAcao acao, String entidade, Long entidadeId, String descricao) {
        LogAuditoria log = new LogAuditoria();
        log.setDataHora(LocalDateTime.now());
        log.setUsuarioId(usuario != null ? usuario.getId() : null);
        log.setUsuarioNome(usuario != null ? usuario.getNome() : "Anônimo/Sistema");
        log.setAcao(acao);
        log.setEntidade(entidade);
        log.setEntidadeId(entidadeId);
        log.setDescricao(descricao);
        log.setIp(obterIpRequisicaoAtual());
        logAuditoriaRepository.save(log);
    }

    private Usuario usuarioAutenticadoAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Usuario usuario) {
            return usuario;
        }
        return null;
    }

    private String obterIpRequisicaoAtual() {
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

    public Page<LogAuditoria> buscar(Long usuarioId, TipoAcao acao, String entidade,
                                      LocalDateTime inicio, LocalDateTime fim, Pageable pageable) {
        Specification<LogAuditoria> spec = Specification.where(null);

        if (usuarioId != null) {
            spec = spec.and((raiz, query, cb) -> cb.equal(raiz.get("usuarioId"), usuarioId));
        }
        if (acao != null) {
            spec = spec.and((raiz, query, cb) -> cb.equal(raiz.get("acao"), acao));
        }
        if (entidade != null && !entidade.isBlank()) {
            spec = spec.and((raiz, query, cb) -> cb.equal(raiz.get("entidade"), entidade));
        }
        if (inicio != null) {
            spec = spec.and((raiz, query, cb) -> cb.greaterThanOrEqualTo(raiz.get("dataHora"), inicio));
        }
        if (fim != null) {
            spec = spec.and((raiz, query, cb) -> cb.lessThanOrEqualTo(raiz.get("dataHora"), fim));
        }

        return logAuditoriaRepository.findAll(spec, pageable);
    }

    public List<String> listarEntidadesDistintas() {
        return logAuditoriaRepository.listarEntidadesDistintas();
    }
}
