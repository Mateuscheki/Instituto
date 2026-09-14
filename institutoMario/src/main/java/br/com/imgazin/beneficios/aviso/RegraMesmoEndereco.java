package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.form.BeneficiarioForm;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import br.com.imgazin.beneficios.util.EnderecoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** MESMO_ENDERECO (ATENCAO): outro beneficiário ativo com a mesma chave de residência. */
@Component
@RequiredArgsConstructor
public class RegraMesmoEndereco implements RegraAviso {

    private final BeneficiarioRepository beneficiarioRepository;

    @Override
    public List<Aviso> avaliar(BeneficiarioForm form) {
        String chave = EnderecoUtils.calcularChaveResidencia(form.getCep(), form.getNumero(), form.getComplemento());
        if (chave.isBlank()) {
            return List.of();
        }

        return beneficiarioRepository.findByEnderecoChaveResidenciaAndAtivoTrue(chave).stream()
                .filter(outro -> !outro.getId().equals(form.getId()))
                .map(outro -> Aviso.atencao(
                        "MESMO_ENDERECO",
                        "Já existe um cadastro ativo (" + outro.getNome() + ") no mesmo endereço.",
                        outro.getId()))
                .toList();
    }
}
