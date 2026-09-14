package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.form.BeneficiarioForm;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** PRAZO_PROXIMO (INFO): prazo final do benefício nos próximos 30 dias. */
@Component
public class RegraPrazoProximo implements RegraAviso {

    private static final int DIAS_LIMITE = 30;

    @Override
    public List<Aviso> avaliar(BeneficiarioForm form) {
        LocalDate prazo = parsear(form.getPrazoFinalBeneficio());
        if (prazo == null) {
            return List.of();
        }

        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(DIAS_LIMITE);
        boolean proximo = !prazo.isBefore(hoje) && !prazo.isAfter(limite);
        if (!proximo) {
            return List.of();
        }

        String prazoFormatado = prazo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return List.of(Aviso.info(
                "PRAZO_PROXIMO",
                "O prazo final do benefício (" + prazoFormatado + ") está próximo do vencimento.",
                null
        ));
    }

    private LocalDate parsear(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valor);
        } catch (Exception e) {
            return null;
        }
    }
}
