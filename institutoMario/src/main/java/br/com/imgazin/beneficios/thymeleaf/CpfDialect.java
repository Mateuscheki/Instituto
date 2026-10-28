package br.com.imgazin.beneficios.thymeleaf;

import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

public class CpfDialect extends AbstractDialect implements IExpressionObjectDialect {

    private static final IExpressionObjectFactory EXPRESSION_OBJECT_FACTORY = new CpfExpressionObjectFactory();

    public CpfDialect() {
        super("Benefícios CPF Dialect");
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return EXPRESSION_OBJECT_FACTORY;
    }
}
