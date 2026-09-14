package br.com.imgazin.beneficios.dto;

import br.com.imgazin.beneficios.domain.RespostaSimNao;
import br.com.imgazin.beneficios.domain.StatusBeneficiario;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Ficha completa do beneficiário (view {@code beneficiarios/ficha}). */
@Data
public class BeneficiarioFichaDto {

    private Long id;
    private String cpf;
    private String nome;
    private LocalDate dataNascimento;
    private String telefone;
    private String email;
    private String nomeMae;
    private String nomeConjuge;
    private String cpfConjuge;

    private Integer pessoasResidencia;
    private RespostaSimNao temFilhos;
    private Integer quantidadeFilhos;
    private String idadesFilhos;
    private RespostaSimNao filhosComDeficiencia;
    private String descricaoDeficienciaFilhos;
    private RespostaSimNao familiarComDeficiencia;
    private String descricaoDeficienciaFamiliar;
    private RespostaSimNao temTrabalho;
    private BigDecimal rendaFamiliar;

    private String enderecoCompleto;
    private String cep;
    private String rua;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;

    private StatusBeneficiario status;
    private LocalDate prazoFinalBeneficio;
    private Long padrinhoId;
    private String padrinhoNome;
    private String observacao;
    private boolean ativo;

    private List<RetiradaResumoDto> ultimasRetiradas;
    private boolean retirouMesAtual;

    private LocalDateTime criadoEm;
    private String criadoPor;
    private LocalDateTime atualizadoEm;
    private String atualizadoPor;
}
