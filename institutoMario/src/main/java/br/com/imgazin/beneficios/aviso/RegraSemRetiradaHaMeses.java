package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.repository.RetiradaCestaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/** SEM_RETIRADA_HA_MESES (INFO): nenhuma retirada válida nos últimos 3 meses ANTES do mês sendo registrado. */
@Component
@RequiredArgsConstructor
public class RegraSemRetiradaHaMeses implements RegraAvisoRetirada {

    private static final int MESES_LIMITE = 3;

    private final RetiradaCestaRepository retiradaCestaRepository;

    @Override
    public List<Aviso> avaliar(ContextoAvaliacaoRetirada contexto) {
        Beneficiario beneficiario = contexto.getBeneficiario();
        LocalDate mes = contexto.getMesReferencia();
        if (beneficiario == null || mes == null) {
            return List.of();
        }

        LocalDate limiteInferior = mes.minusMonths(MESES_LIMITE);
        boolean teveRetiradaRecente = retiradaCestaRepository
                .findByBeneficiarioIdAndCanceladaFalseOrderByMesReferenciaDesc(beneficiario.getId()).stream()
                .anyMatch(retirada -> !retirada.getMesReferencia().isBefore(limiteInferior)
                        && retirada.getMesReferencia().isBefore(mes));

        if (teveRetiradaRecente) {
            return List.of();
        }

        return List.of(Aviso.info("SEM_RETIRADA_HA_MESES",
                "Este beneficiário está sem retirada válida nos últimos " + MESES_LIMITE + " meses.",
                beneficiario.getId()));
    }
}
