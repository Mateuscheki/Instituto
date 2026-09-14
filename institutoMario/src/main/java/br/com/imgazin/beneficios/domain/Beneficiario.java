package br.com.imgazin.beneficios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import br.com.imgazin.beneficios.validation.Cpf;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pessoa cadastrada no programa de cestas básicas. O endereço mora em
 * {@link BeneficiarioEndereco} (1:1, tabela própria); o lado inverso é
 * mapeado aqui (LAZY) só para sustentar a consulta derivada
 * {@code findByEnderecoChaveResidenciaAndAtivoTrue}.
 * <p>
 * {@code @Getter}/{@code @Setter} em vez de {@code @Data}: entidade JPA com
 * relacionamentos (padrinho, endereço) não deve gerar {@code equals}/
 * {@code hashCode}/{@code toString} automáticos — arrisca
 * {@code StackOverflowError} em referência bidirecional e força carga de
 * proxy LAZY sem necessidade.
 */
@Getter
@Setter
@Entity
@Table(name = "beneficiario")
public class Beneficiario extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "CPF é obrigatório")
    @Cpf
    @Column(name = "cpf", nullable = false, length = 11, unique = true)
    private String cpf;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150)
    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Size(max = 20)
    @Column(name = "telefone", length = 20)
    private String telefone;

    @Size(max = 150)
    @Column(name = "email", length = 150)
    private String email;

    @Size(max = 150)
    @Column(name = "nome_mae", length = 150)
    private String nomeMae;

    @Size(max = 150)
    @Column(name = "nome_conjuge", length = 150)
    private String nomeConjuge;

    @Cpf(message = "CPF do cônjuge inválido")
    @Column(name = "cpf_conjuge", length = 11)
    private String cpfConjuge;

    @Column(name = "pessoas_residencia")
    private Integer pessoasResidencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tem_filhos", length = 20)
    private RespostaSimNao temFilhos;

    @Column(name = "quantidade_filhos")
    private Integer quantidadeFilhos;

    @Column(name = "idades_filhos", columnDefinition = "TEXT")
    private String idadesFilhos;

    @Enumerated(EnumType.STRING)
    @Column(name = "filhos_com_deficiencia", length = 20)
    private RespostaSimNao filhosComDeficiencia;

    @Column(name = "descricao_deficiencia_filhos", columnDefinition = "TEXT")
    private String descricaoDeficienciaFilhos;

    @Enumerated(EnumType.STRING)
    @Column(name = "familiar_com_deficiencia", length = 20)
    private RespostaSimNao familiarComDeficiencia;

    @Column(name = "descricao_deficiencia_familiar", columnDefinition = "TEXT")
    private String descricaoDeficienciaFamiliar;

    @Enumerated(EnumType.STRING)
    @Column(name = "tem_trabalho", length = 20)
    private RespostaSimNao temTrabalho;

    @Column(name = "renda_familiar", precision = 10, scale = 2)
    private BigDecimal rendaFamiliar;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private StatusBeneficiario status;

    @Column(name = "prazo_final_beneficio")
    private LocalDate prazoFinalBeneficio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padrinho_id")
    private Voluntario padrinho;

    @OneToOne(mappedBy = "beneficiario", fetch = FetchType.LAZY)
    private BeneficiarioEndereco endereco;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;
}
