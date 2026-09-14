package br.com.imgazin.beneficios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import br.com.imgazin.beneficios.validation.Cpf;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Pessoa que colabora com o Instituto — entrega, eventos, triagem — podendo
 * também ser "padrinho/madrinha" de um ou mais beneficiários.
 */
@Getter
@Setter
@Entity
@Table(name = "voluntario")
public class Voluntario extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Cpf
    @Column(name = "cpf", length = 11, unique = true)
    private String cpf;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150)
    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Size(max = 20)
    @Column(name = "telefone", length = 20)
    private String telefone;

    @Size(max = 150)
    @Column(name = "email", length = 150)
    private String email;

    @Size(max = 120)
    @Column(name = "area_atuacao", length = 120)
    private String areaAtuacao;

    @Size(max = 120)
    @Column(name = "setor", length = 120)
    private String setor;

    @Column(name = "is_padrinho", nullable = false)
    private boolean padrinho = false;

    @Size(max = 200)
    @Column(name = "disponibilidade", length = 200)
    private String disponibilidade;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;
}
