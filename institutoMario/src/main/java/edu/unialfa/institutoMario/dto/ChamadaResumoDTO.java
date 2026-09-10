package edu.unialfa.institutoMario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

/** Linha resumida de uma chamada para a tela de listagem (`/chamadas`). */
@Data
@AllArgsConstructor
public class ChamadaResumoDTO {
    private Long id;
    private String turmaNome;
    private LocalDate data;
    private long totalAlunos;
    private long totalPresentes;
}
