package br.com.imgazin.beneficios.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** Tela `voluntarios/atuacao`: entregas por mês, apadrinhados, última atividade. */
@Data
public class VoluntarioAtuacaoDto {
    private Long id;
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private String areaAtuacao;
    private String setor;
    private String disponibilidade;
    private String observacao;
    private boolean padrinho;
    private boolean ativo;

    private LocalDateTime ultimaAtividade;
    private List<EntregaMesDto> entregasPorMes;
    private List<ApadrinhadoResumoDto> apadrinhados;
}
