package br.com.imgazin.beneficios.aviso;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.form.BeneficiarioForm;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import br.com.imgazin.beneficios.util.CpfUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CPF_JA_E_CONJUGE (ALTA): o CPF (ou, na falta dele, o nome) desta pessoa já
 * consta como cônjuge no cadastro de outro beneficiário ativo.
 */
@Component
@RequiredArgsConstructor
public class RegraCpfJaEConjuge implements RegraAviso {

    private final BeneficiarioRepository beneficiarioRepository;

    @Override
    public List<Aviso> avaliar(BeneficiarioForm form) {
        List<Beneficiario> encontrados = new ArrayList<>();

        String cpfNormalizado = CpfUtils.normalizar(form.getCpf());
        if (cpfNormalizado != null && cpfNormalizado.length() == 11) {
            encontrados.addAll(beneficiarioRepository.findByCpfConjugeAndAtivoTrue(cpfNormalizado));
        }

        if (form.getNome() != null && !form.getNome().isBlank()) {
            encontrados.addAll(beneficiarioRepository.findByNomeConjugeIgnoreCaseAndAtivoTrue(form.getNome().trim()));
        }

        // distinct() por id, não por identidade de objeto: as duas buscas acima podem
        // devolver a mesma pessoa como instâncias JPA diferentes.
        Map<Long, Beneficiario> semDuplicados = new LinkedHashMap<>();
        for (Beneficiario encontrado : encontrados) {
            semDuplicados.putIfAbsent(encontrado.getId(), encontrado);
        }

        return semDuplicados.values().stream()
                .filter(outro -> !outro.getId().equals(form.getId()))
                .map(outro -> Aviso.alta(
                        "CPF_JA_E_CONJUGE",
                        "Esta pessoa já consta como cônjuge no cadastro de " + outro.getNome() + ".",
                        outro.getId()))
                .toList();
    }
}
