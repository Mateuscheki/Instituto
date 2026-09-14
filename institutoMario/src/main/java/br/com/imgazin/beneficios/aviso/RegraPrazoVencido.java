package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.form.BeneficiarioForm;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** PRAZO_VENCIDO (ATENCAO): prazo final do benefício anterior a hoje. */
@Component
public class RegraPrazoVencido implements RegraAviso {

    @Override
    public List<Aviso> avaliar(BeneficiarioForm form) {
        LocalDate prazo = parsear(form.getPrazoFinalBeneficio());
        if (prazo == null || !prazo.isBefore(LocalDate.now())) {
            return List.of();
        }

        String prazoFormatado = prazo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return List.of(Aviso.atencao(
                "PRAZO_VENCIDO",
                "O prazo final do benefício (" + prazoFormatado + ") já passou.",
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
