package br.com.imgazin.beneficios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Endereço do beneficiário (1:1). {@code chaveResidencia} é um valor
 * derivado e normalizado (cep + número + complemento, minúsculo, sem acento,
 * sem espaço) usado para detectar famílias no mesmo endereço — a lógica de
 * normalização em si é regra de negócio e fica para uma etapa seguinte
 * (aqui só existe a coluna e o índice).
 */
@Getter
@Setter
@Entity
@Table(name = "beneficiario_endereco")
public class BeneficiarioEndereco extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Beneficiário é obrigatório")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiario_id", nullable = false, unique = true)
    private Beneficiario beneficiario;

    @Column(name = "endereco_completo", columnDefinition = "TEXT")
    private String enderecoCompleto;

    @Size(max = 8)
    @Column(name = "cep", length = 8)
    private String cep;

    @Size(max = 150)
    @Column(name = "rua", length = 150)
    private String rua;

    @Size(max = 20)
    @Column(name = "numero", length = 20)
    private String numero;

    @Size(max = 100)
    @Column(name = "complemento", length = 100)
    private String complemento;

    @Size(max = 100)
    @Column(name = "bairro", length = 100)
    private String bairro;

    @Size(max = 100)
    @Column(name = "cidade", length = 100)
    private String cidade;

    @Size(max = 2)
    @Column(name = "uf", length = 2)
    private String uf;

    @Size(max = 200)
    @Column(name = "chave_residencia", length = 200)
    private String chaveResidencia;
}
