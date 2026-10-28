package br.com.imgazin.beneficios.form;

import lombok.Data;

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
