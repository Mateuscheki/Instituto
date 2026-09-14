package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.form.RetiradaForm;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Entrada do motor de avisos de retirada: o formulário + o beneficiário já
 * resolvido (por CPF) + o mês de referência já convertido para
 * {@link LocalDate} (sempre o dia 1º — ver CLAUDE.md do módulo).
 */
public class ContextoAvaliacaoRetirada {

    private final RetiradaForm form;
    private final Beneficiario beneficiario;
    private final LocalDate mesReferencia;

    public ContextoAvaliacaoRetirada(RetiradaForm form, Beneficiario beneficiario) {
        this.form = form;
        this.beneficiario = beneficiario;
        this.mesReferencia = parsearMes(form.getMesReferencia());
    }

    private static LocalDate parsearMes(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return YearMonth.parse(valor).atDay(1);
        } catch (Exception e) {
            return null;
        }
    }

    public RetiradaForm getForm() {
        return form;
    }

    public Beneficiario getBeneficiario() {
        return beneficiario;
    }

    public LocalDate getMesReferencia() {
        return mesReferencia;
    }
}
