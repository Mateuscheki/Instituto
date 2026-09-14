package br.com.imgazin.beneficios.mapper;

import br.com.imgazin.beneficios.domain.Voluntario;
import br.com.imgazin.beneficios.dto.ApadrinhadoResumoDto;
import br.com.imgazin.beneficios.dto.EntregaMesDto;
import br.com.imgazin.beneficios.dto.VoluntarioAtuacaoDto;
import br.com.imgazin.beneficios.dto.VoluntarioListaItemDto;
import br.com.imgazin.beneficios.form.VoluntarioForm;
import br.com.imgazin.beneficios.util.CpfUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class VoluntarioMapper {

    public VoluntarioForm paraForm(Voluntario voluntario) {
        VoluntarioForm form = new VoluntarioForm();
        form.setId(voluntario.getId());
        form.setNome(voluntario.getNome());
        form.setCpf(voluntario.getCpf() == null ? null : CpfUtils.formatar(voluntario.getCpf()));
        form.setTelefone(voluntario.getTelefone());
        form.setEmail(voluntario.getEmail());
        form.setAreaAtuacao(voluntario.getAreaAtuacao());
        form.setSetor(voluntario.getSetor());
        form.setPadrinho(Boolean.toString(voluntario.isPadrinho()));
        form.setDisponibilidade(voluntario.getDisponibilidade());
        form.setObservacao(voluntario.getObservacao());
        return form;
    }

    public void aplicarNaEntidade(VoluntarioForm form, Voluntario entidade) {
        entidade.setNome(form.getNome().trim());
        entidade.setCpf(blankToNull(form.getCpf()) == null ? null : CpfUtils.normalizar(form.getCpf()));
        entidade.setTelefone(blankToNull(form.getTelefone()));
        entidade.setEmail(blankToNull(form.getEmail()));
        entidade.setAreaAtuacao(blankToNull(form.getAreaAtuacao()));
        entidade.setSetor(blankToNull(form.getSetor()));
        entidade.setPadrinho("true".equalsIgnoreCase(form.getPadrinho()));
        entidade.setDisponibilidade(blankToNull(form.getDisponibilidade()));
        entidade.setObservacao(blankToNull(form.getObservacao()));
    }

    public VoluntarioListaItemDto paraItemLista(Voluntario voluntario) {
        return new VoluntarioListaItemDto(
                voluntario.getId(),
                voluntario.getNome(),
                voluntario.getCpf(),
                voluntario.getTelefone(),
                voluntario.getAreaAtuacao(),
                voluntario.getSetor(),
                voluntario.isPadrinho(),
                voluntario.isAtivo()
        );
    }

    public VoluntarioAtuacaoDto paraAtuacao(Voluntario voluntario, List<EntregaMesDto> entregasPorMes,
                                             List<ApadrinhadoResumoDto> apadrinhados, LocalDateTime ultimaAtividade) {
        VoluntarioAtuacaoDto dto = new VoluntarioAtuacaoDto();
        dto.setId(voluntario.getId());
        dto.setNome(voluntario.getNome());
        dto.setCpf(voluntario.getCpf());
        dto.setTelefone(voluntario.getTelefone());
        dto.setEmail(voluntario.getEmail());
        dto.setAreaAtuacao(voluntario.getAreaAtuacao());
        dto.setSetor(voluntario.getSetor());
        dto.setDisponibilidade(voluntario.getDisponibilidade());
        dto.setObservacao(voluntario.getObservacao());
        dto.setPadrinho(voluntario.isPadrinho());
        dto.setAtivo(voluntario.isAtivo());
        dto.setUltimaAtividade(ultimaAtividade);
        dto.setEntregasPorMes(entregasPorMes);
        dto.setApadrinhados(apadrinhados);
        return dto;
    }

    private String blankToNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
