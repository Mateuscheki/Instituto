package br.com.imgazin.beneficios.form;

import lombok.Data;

/** Só existe para repostar os hidden na tela de confirmação (ver fragments/campos.html :: hiddensDoForm). */
@Data
public class VincularPadrinhoForm {
    private String beneficiarioId;
    private String voluntarioId;
}
