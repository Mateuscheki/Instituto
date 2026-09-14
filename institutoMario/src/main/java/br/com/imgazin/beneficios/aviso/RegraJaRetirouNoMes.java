package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.repository.RetiradaCestaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** JA_RETIROU_NO_MES (ALTA): já existe retirada válida deste beneficiário no mês. */
@Component
@RequiredArgsConstructor
public class RegraJaRetirouNoMes implements RegraAvisoRetirada {

    private final RetiradaCestaRepository retiradaCestaRepository;

    @Override
    public List<Aviso> avaliar(ContextoAvaliacaoRetirada contexto) {
        if (contexto.getBeneficiario() == null || contexto.getMesReferencia() == null) {
            return List.of();
        }

        return retiradaCestaRepository
                .findByBeneficiarioIdAndMesReferenciaAndCanceladaFalse(contexto.getBeneficiario().getId(), contexto.getMesReferencia())
                .map(existente -> List.of(Aviso.alta(
                        "JA_RETIROU_NO_MES",
                        "Já existe uma retirada válida deste beneficiário em " + contexto.getMesReferencia() + ".",
                        existente.getId())))
                .orElseGet(List::of);
    }
}
