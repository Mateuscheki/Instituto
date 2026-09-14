package br.com.imgazin.beneficios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VoluntarioListaItemDto {
    private Long id;
    private String nome;
    private String cpf;
    private String telefone;
    private String areaAtuacao;
    private String setor;
    private boolean padrinho;
    private boolean ativo;
}
