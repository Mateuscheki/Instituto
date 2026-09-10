package edu.unialfa.institutoMario.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Intercepta os métodos de serviço anotados com {@link Auditar} e grava um registro
 * no log de auditoria depois que a ação é concluída com sucesso. Se o método lançar
 * exceção, nada é registrado (a ação não chegou a acontecer). Se o próprio registro
 * do log falhar por algum motivo, o erro é apenas logado — nunca derruba a operação
 * de negócio que estava sendo auditada.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditoriaAspect {

    private final LogAuditoriaService logAuditoriaService;

    @Around("@annotation(auditar)")
    public Object aoRedor(ProceedingJoinPoint joinPoint, Auditar auditar) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Object primeiroArgumento = args.length > 0 ? args[0] : null;
        Long idAntes = extrairId(primeiroArgumento);

        Object resultado = joinPoint.proceed();

        TipoAcao acao;
        Long entidadeId;

        if (auditar.acao() == TipoAcao.EXCLUSAO) {
            acao = TipoAcao.EXCLUSAO;
            entidadeId = idAntes; // deletarPorId(Long id): o próprio argumento é o id excluído
        } else {
            // salvar(Entidade e): decide criação x atualização pelo id ANTES de proceder
            acao = (idAntes != null) ? TipoAcao.ATUALIZACAO : TipoAcao.CRIACAO;
            Long idDepois = extrairId(primeiroArgumento); // JPA preenche o id gerado no próprio objeto
            entidadeId = (idDepois != null) ? idDepois : extrairId(resultado);
        }

        String descricao = auditar.entidade() + " " + textoAcao(acao)
                + (entidadeId != null ? " (ID " + entidadeId + ")" : "");

        try {
            logAuditoriaService.registrar(acao, auditar.entidade(), entidadeId, descricao);
        } catch (Exception e) {
            log.error("Falha ao registrar log de auditoria para a entidade '{}'", auditar.entidade(), e);
        }

        return resultado;
    }

    private Long extrairId(Object objeto) {
        if (objeto == null) {
            return null;
        }
        if (objeto instanceof Long id) {
            return id;
        }
        if (objeto instanceof Number numero) {
            return numero.longValue();
        }
        try {
            Method getId = objeto.getClass().getMethod("getId");
            Object valor = getId.invoke(objeto);
            return (valor instanceof Long) ? (Long) valor : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String textoAcao(TipoAcao acao) {
        return switch (acao) {
            case CRIACAO -> "criado(a)";
            case ATUALIZACAO -> "atualizado(a)";
            case EXCLUSAO -> "excluído(a)";
            default -> acao.name();
        };
    }
}
