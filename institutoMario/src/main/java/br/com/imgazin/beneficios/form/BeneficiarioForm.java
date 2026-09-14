package br.com.imgazin.beneficios.form;

import lombok.Data;

/**
 * Objeto de formulário do cadastro de beneficiário. Todo campo é String de
 * propósito — inclusive datas e números — para que um valor mal digitado
 * NUNCA seja perdido ao re-renderizar a tela com erro (ver
 * {@link br.com.imgazin.beneficios.validation.BeneficiarioFormValidator}, que
 * faz a conversão e reporta erro de formato campo a campo). Entidade JPA
 * nunca vai para a view — este Form (e os DTOs de leitura) é o que trafega
 * entre controller e view.
 */
@Data
public class BeneficiarioForm {

    private Long id;

    // Identificação
    private String cpf;
    private String nome;
    private String dataNascimento;
    private String telefone;
    private String email;
    private String nomeMae;
    private String nomeConjuge;
    private String cpfConjuge;

    // Composição familiar
    private String pessoasResidencia;
    private String temFilhos;
    private String quantidadeFilhos;
    private String idadesFilhos;
    private String filhosComDeficiencia;
    private String descricaoDeficienciaFilhos;
    private String familiarComDeficiencia;
    private String descricaoDeficienciaFamiliar;
    private String temTrabalho;
    private String rendaFamiliar;

    // Endereço
    private String enderecoCompleto;
    private String cep;
    private String rua;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;

    // Benefício
    private String padrinhoId;
    private String prazoFinalBeneficio;
    private String status;
    private String observacao;
}
