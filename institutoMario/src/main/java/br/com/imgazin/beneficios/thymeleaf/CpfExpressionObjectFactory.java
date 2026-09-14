package br.com.imgazin.beneficios.thymeleaf;

import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.Collections;
import java.util.Set;

public class CpfExpressionObjectFactory implements IExpressionObjectFactory {

    private static final String CPF_EXPRESSION_OBJECT_NAME = "cpf";
    private static final Set<String> ALL_EXPRESSION_OBJECT_NAMES = Collections.singleton(CPF_EXPRESSION_OBJECT_NAME);

    @Override
    public Set<String> getAllExpressionObjectNames() {
        return ALL_EXPRESSION_OBJECT_NAMES;
    }

    @Override
    public Object buildObject(IExpressionContext context, String expressionObjectName) {
        if (CPF_EXPRESSION_OBJECT_NAME.equals(expressionObjectName)) {
            return new CpfExpressionUtility();
        }
        return null;
    }

    @Override
    public boolean isCacheable(String expressionObjectName) {
        // Não guarda estado (só lê o SecurityContext a cada chamada), então
        // uma instância por render é irrelevante — mas não pode ser cacheada
        // entre requisições porque o usuário autenticado muda a cada uma.
        return false;
    }
}
