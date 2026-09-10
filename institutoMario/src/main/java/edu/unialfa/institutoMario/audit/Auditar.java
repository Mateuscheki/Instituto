package edu.unialfa.institutoMario.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca um método de serviço cuja execução (bem-sucedida) deve gerar um registro
 * no log de auditoria. Pensado para dois formatos de método:
 *
 * <ul>
 *     <li>{@code acao = CRIACAO} em métodos {@code salvar(Entidade e)}: o aspecto
 *     verifica o id da entidade antes de executar o método para decidir sozinho se
 *     o resultado foi uma criação ou uma atualização.</li>
 *     <li>{@code acao = EXCLUSAO} em métodos {@code deletarPorId(Long id)}: o próprio
 *     argumento já é o id do registro removido.</li>
 * </ul>
 *
 * Ações fora desse padrão (ex.: login, redefinição de senha, correção de prova) são
 * registradas manualmente via {@link LogAuditoriaService}, sem esta anotação.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditar {

    TipoAcao acao();

    String entidade();
}
