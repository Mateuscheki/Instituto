package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.form.BeneficiarioForm;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * NOME_JA_E_MAE (ALTA): o nome desta pessoa já consta como "nome da mãe" no
 * cadastro de outro beneficiário ativo.
 */
@Component
@RequiredArgsConstructor
public class RegraNomeJaEMae implements RegraAviso {

    private final BeneficiarioRepository beneficiarioRepository;

    @Override
    public List<Aviso> avaliar(BeneficiarioForm form) {
        String nome = form.getNome();
        if (nome == null || nome.isBlank()) {
            return List.of();
        }

        return beneficiarioRepository.findByNomeMaeIgnoreCaseAndAtivoTrue(nome.trim()).stream()
                .filter(outro -> !outro.getId().equals(form.getId()))
                .map(outro -> Aviso.alta(
                        "NOME_JA_E_MAE",
                        "Este nome já consta como nome da mãe no cadastro de " + outro.getNome() + ".",
                        outro.getId()))
                .toList();
    }
}
