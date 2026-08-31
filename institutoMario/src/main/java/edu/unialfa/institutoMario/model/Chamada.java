package edu.unialfa.institutoMario.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor      // Mantemos este, pois o JPA/Hibernate exige um construtor vazio
@Entity
public class Chamada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate data;

    @ManyToOne
    @JoinColumn(name = "turma_id")
    private Turma turma;

    // Criamos manualmente o construtor que o ChamadaService precisa
    // Note que deixamos o 'id' de fora, pois o banco de dados cuidará dele
    public Chamada(LocalDate data, Turma turma) {
        this.data = data;
        this.turma = turma;
    }
}