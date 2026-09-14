package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import br.com.imgazin.beneficios.repository.RetiradaCestaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/** MESMA_RESIDENCIA_JA_RETIROU (ATENCAO): outro beneficiário da mesma residência já retirou no mês. */
@Component
@RequiredArgsConstructor
public class RegraMesmaResidenciaJaRetirou implements RegraAvisoRetirada {

    private final BeneficiarioRepository beneficiarioRepository;
    private final RetiradaCestaRepository retiradaCestaRepository;

    @Override
    public List<Aviso> avaliar(ContextoAvaliacaoRetirada contexto) {
        Beneficiario atual = contexto.getBeneficiario();
        LocalDate mes = contexto.getMesReferencia();
        if (atual == null || mes == null || atual.getEndereco() == null) {
            return List.of();
        }

        String chave = atual.getEndereco().getChaveResidencia();
        if (chave == null || chave.isBlank()) {
            return List.of();
        }

        return beneficiarioRepository.findByEnderecoChaveResidenciaAndAtivoTrue(chave).stream()
                .filter(outro -> !outro.getId().equals(atual.getId()))
                .filter(outro -> retiradaCestaRepository.existsByBeneficiarioIdAndMesReferenciaAndCanceladaFalse(outro.getId(), mes))
                .map(outro -> Aviso.atencao(
                        "MESMA_RESIDENCIA_JA_RETIROU",
                        "Outro beneficiário do mesmo endereço (" + outro.getNome() + ") já retirou neste mês.",
                        outro.getId()))
                .toList();
    }
}
