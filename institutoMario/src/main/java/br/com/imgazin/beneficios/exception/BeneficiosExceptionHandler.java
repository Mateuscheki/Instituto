package br.com.imgazin.beneficios.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Tratamento de erro do módulo Benefícios: nunca mostra stack trace pro
 * operador (LGPD + UX), sempre registra no log — sem CPF/telefone/endereço
 * no log, mas a exceção crua pode conter esses dados na mensagem, então
 * logamos só a stack trace via o objeto da exceção (o logger não imprime o
 * `toString()` de entidades do domínio em lugar nenhum deste handler).
 */
@ControllerAdvice(basePackages = "br.com.imgazin.beneficios.controller")
public class BeneficiosExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(BeneficiosExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String tratarErroGeral(Exception excecao, Model model) {
        log.error("Erro não tratado no módulo Benefícios", excecao);

        model.addAttribute("tituloPagina", "Erro");
        model.addAttribute("mensagemErro",
                "Ocorreu um erro inesperado ao processar sua solicitação. " +
                        "Tente novamente em instantes ou avise o suporte técnico.");

        return "beneficios/erro";
    }
}
