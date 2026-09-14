package br.com.imgazin.beneficios.form;

import lombok.Data;

/**
 * Objeto de formulário do registro de retirada. Mesma lógica de
 * {@link BeneficiarioForm}: tudo String, ninguém perde o que digitou num
 * erro de conversão. Nunca tem {@code id} — retirada não se edita, só se
 * cancela e se registra outra (ver CLAUDE.md do módulo, Etapa 3).
 */
@Data
public class RetiradaForm {

    private String cpfBeneficiario;
    private String mesReferencia;
    private String dataRetirada;
    private String quantidadeCestas;
    private String tipoRetirante;
    private String retiranteNome;
    private String retiranteCpf;
    private String retiranteVinculo;
    private String padrinhoId;
    private String voluntarioEntregaId;
    private String observacao;
}
