package br.com.imgazin.beneficios.aviso;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/** RETIRADA_RETROATIVA (INFO): mês de referência diferente do mês corrente. */
@Component
public class RegraRetiradaRetroativa implements RegraAvisoRetirada {

    @Override
    public List<Aviso> avaliar(ContextoAvaliacaoRetirada contexto) {
        LocalDate mes = contexto.getMesReferencia();
        if (mes == null) {
            return List.of();
        }
        LocalDate mesAtual = LocalDate.now().withDayOfMonth(1);
        if (mes.equals(mesAtual)) {
            return List.of();
        }
        return List.of(Aviso.info("RETIRADA_RETROATIVA",
                "Esta retirada está sendo registrada para um mês diferente do atual.", null));
    }
}
