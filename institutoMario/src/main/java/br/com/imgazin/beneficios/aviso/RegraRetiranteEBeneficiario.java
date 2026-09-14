package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import br.com.imgazin.beneficios.util.CpfUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** RETIRANTE_E_BENEFICIARIO (ATENCAO): o CPF do retirante pertence a outro beneficiário ativo. */
@Component
@RequiredArgsConstructor
public class RegraRetiranteEBeneficiario implements RegraAvisoRetirada {

    private final BeneficiarioRepository beneficiarioRepository;

    @Override
    public List<Aviso> avaliar(ContextoAvaliacaoRetirada contexto) {
        String cpfRetirante = CpfUtils.normalizar(contexto.getForm().getRetiranteCpf());
        if (cpfRetirante == null || cpfRetirante.length() != 11) {
            return List.of();
        }

        Beneficiario atual = contexto.getBeneficiario();

        return beneficiarioRepository.findByCpf(cpfRetirante)
                .filter(Beneficiario::isAtivo)
                .filter(outro -> atual == null || !outro.getId().equals(atual.getId()))
                .map(outro -> List.of(Aviso.atencao(
                        "RETIRANTE_E_BENEFICIARIO",
                        "O CPF do retirante pertence a outro beneficiário ativo do programa: " + outro.getNome() + ".",
                        outro.getId())))
                .orElseGet(List::of);
    }
}
