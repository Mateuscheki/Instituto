package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.form.BeneficiarioForm;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** MESMO_NOME_MAE (ATENCAO): outro beneficiário ativo com a mesma mãe (provável irmão). */
@Component
@RequiredArgsConstructor
public class RegraMesmoNomeMae implements RegraAviso {

    private final BeneficiarioRepository beneficiarioRepository;

    @Override
    public List<Aviso> avaliar(BeneficiarioForm form) {
        String nomeMae = form.getNomeMae();
        if (nomeMae == null || nomeMae.isBlank()) {
            return List.of();
        }

        return beneficiarioRepository.findByNomeMaeIgnoreCaseAndAtivoTrue(nomeMae.trim()).stream()
                .filter(outro -> !outro.getId().equals(form.getId()))
                .map(outro -> Aviso.atencao(
                        "MESMO_NOME_MAE",
                        "Já existe um cadastro ativo (" + outro.getNome() + ") com a mesma mãe informada — possível irmão.",
                        outro.getId()))
                .toList();
    }
}
