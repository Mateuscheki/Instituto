package br.com.imgazin.beneficios.service;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.aviso.ContextoAvaliacaoRetirada;
import br.com.imgazin.beneficios.aviso.RegraAvisoRetirada;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidacaoRetiradaService {

    private final List<RegraAvisoRetirada> regras;

    public List<Aviso> avaliar(ContextoAvaliacaoRetirada contexto) {
        List<Aviso> avisos = new ArrayList<>();
        for (RegraAvisoRetirada regra : regras) {
            avisos.addAll(regra.avaliar(contexto));
        }
        return avisos;
    }
}
