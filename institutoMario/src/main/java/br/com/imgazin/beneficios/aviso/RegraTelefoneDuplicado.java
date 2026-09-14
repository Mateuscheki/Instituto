package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.form.BeneficiarioForm;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** TELEFONE_DUPLICADO (INFO): telefone já usado por outro cadastro ativo. */
@Component
@RequiredArgsConstructor
public class RegraTelefoneDuplicado implements RegraAviso {

    private final BeneficiarioRepository beneficiarioRepository;

    @Override
    public List<Aviso> avaliar(BeneficiarioForm form) {
        String telefone = form.getTelefone();
        if (telefone == null || telefone.isBlank()) {
            return List.of();
        }

        return beneficiarioRepository.findByTelefoneAndAtivoTrue(telefone.trim()).stream()
                .filter(outro -> !outro.getId().equals(form.getId()))
                .map(outro -> Aviso.info(
                        "TELEFONE_DUPLICADO",
                        "Este telefone também está cadastrado para " + outro.getNome() + ".",
                        outro.getId()))
                .toList();
    }
}
