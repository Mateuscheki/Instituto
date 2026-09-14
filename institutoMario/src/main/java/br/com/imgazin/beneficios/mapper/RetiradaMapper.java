package br.com.imgazin.beneficios.mapper;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.RetiradaCesta;
import br.com.imgazin.beneficios.domain.RetiradaConfirmacao;
import br.com.imgazin.beneficios.domain.SituacaoElegibilidade;
import br.com.imgazin.beneficios.domain.TipoRetirante;
import br.com.imgazin.beneficios.domain.Voluntario;
import br.com.imgazin.beneficios.dto.ElegibilidadeDto;
import br.com.imgazin.beneficios.dto.RetiradaConfirmacaoDto;
import br.com.imgazin.beneficios.dto.RetiradaDetalheDto;
import br.com.imgazin.beneficios.dto.RetiradaListaItemDto;
import br.com.imgazin.beneficios.form.RetiradaForm;
import br.com.imgazin.beneficios.util.CpfUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Component
public class RetiradaMapper {

    /** Monta a entidade a partir do form já validado — não persiste, não decide status do beneficiário. */
    public RetiradaCesta paraEntidade(RetiradaForm form, Beneficiario beneficiario, Voluntario padrinho,
                                       Voluntario voluntarioEntrega, String registradoPor) {
        RetiradaCesta retirada = new RetiradaCesta();
        retirada.setBeneficiario(beneficiario);
        retirada.setMesReferencia(YearMonth.parse(form.getMesReferencia()).atDay(1));
        retirada.setDataRetirada(LocalDateTime.parse(form.getDataRetirada()));
        retirada.setQuantidadeCestas(Integer.valueOf(form.getQuantidadeCestas().trim()));

        TipoRetirante tipo = TipoRetirante.valueOf(form.getTipoRetirante());
        retirada.setTipoRetirante(tipo);
        retirada.setRetiranteNome(form.getRetiranteNome().trim());
        retirada.setRetiranteCpf(blankToNull(form.getRetiranteCpf()) == null ? null : CpfUtils.normalizar(form.getRetiranteCpf()));
        retirada.setRetiranteVinculo(blankToNull(form.getRetiranteVinculo()));

        retirada.setRetiradoPorPadrinho(tipo == TipoRetirante.PADRINHO);
        retirada.setPadrinho(tipo == TipoRetirante.PADRINHO ? padrinho : null);
        retirada.setVoluntarioEntrega(voluntarioEntrega);

        retirada.setRegistradoPor(registradoPor);
        retirada.setObservacao(blankToNull(form.getObservacao()));
        retirada.setCancelada(false);
        return retirada;
    }

    public ElegibilidadeDto paraElegibilidade(Beneficiario beneficiario, LocalDate mesReferencia,
                                               SituacaoElegibilidade situacao, RetiradaCesta ultimaRetiradaValida,
                                               List<Aviso> avisosPreliminares) {
        ElegibilidadeDto dto = new ElegibilidadeDto();
        dto.setBeneficiarioId(beneficiario.getId());
        dto.setNome(beneficiario.getNome());
        dto.setCpf(beneficiario.getCpf());
        dto.setNomeConjuge(beneficiario.getNomeConjuge());
        dto.setNomeMae(beneficiario.getNomeMae());
        dto.setStatus(beneficiario.getStatus());
        dto.setMesReferencia(mesReferencia);
        dto.setSituacao(situacao);
        dto.setPrazoFinalBeneficio(beneficiario.getPrazoFinalBeneficio());

        if (ultimaRetiradaValida != null) {
            dto.setUltimaRetiradaData(ultimaRetiradaValida.getDataRetirada());
            dto.setUltimaRetiradaTipoRetirante(ultimaRetiradaValida.getTipoRetirante());
        }

        if (beneficiario.getPadrinho() != null) {
            dto.setPadrinhoVinculadoId(beneficiario.getPadrinho().getId());
            dto.setPadrinhoVinculadoNome(beneficiario.getPadrinho().getNome());
        }

        dto.setAvisosPreliminares(avisosPreliminares);
        return dto;
    }

    public RetiradaListaItemDto paraItemLista(RetiradaCesta retirada) {
        Beneficiario beneficiario = retirada.getBeneficiario();
        return new RetiradaListaItemDto(
                retirada.getId(),
                beneficiario.getId(),
                beneficiario.getNome(),
                beneficiario.getCpf(),
                retirada.getMesReferencia(),
                retirada.getDataRetirada(),
                retirada.getTipoRetirante(),
                retirada.getRetiranteNome(),
                retirada.isRetiradoPorPadrinho(),
                retirada.getPadrinho() == null ? null : retirada.getPadrinho().getNome(),
                retirada.isCancelada()
        );
    }

    public RetiradaDetalheDto paraDetalhe(RetiradaCesta retirada, List<RetiradaConfirmacao> confirmacoes) {
        Beneficiario beneficiario = retirada.getBeneficiario();

        RetiradaDetalheDto dto = new RetiradaDetalheDto();
        dto.setId(retirada.getId());
        dto.setBeneficiarioId(beneficiario.getId());
        dto.setBeneficiarioNome(beneficiario.getNome());
        dto.setBeneficiarioCpf(beneficiario.getCpf());
        dto.setMesReferencia(retirada.getMesReferencia());
        dto.setDataRetirada(retirada.getDataRetirada());
        dto.setQuantidadeCestas(retirada.getQuantidadeCestas());
        dto.setTipoRetirante(retirada.getTipoRetirante());
        dto.setRetiranteNome(retirada.getRetiranteNome());
        dto.setRetiranteCpf(retirada.getRetiranteCpf());
        dto.setRetiranteVinculo(retirada.getRetiranteVinculo());
        dto.setRetiradoPorPadrinho(retirada.isRetiradoPorPadrinho());
        dto.setPadrinhoNome(retirada.getPadrinho() == null ? null : retirada.getPadrinho().getNome());
        dto.setVoluntarioEntregaNome(retirada.getVoluntarioEntrega() == null ? null : retirada.getVoluntarioEntrega().getNome());
        dto.setRegistradoPor(retirada.getRegistradoPor());
        dto.setObservacao(retirada.getObservacao());
        dto.setCancelada(retirada.isCancelada());
        dto.setMotivoCancelamento(retirada.getMotivoCancelamento());
        dto.setCanceladaEm(retirada.getCanceladaEm());
        dto.setCanceladaPor(retirada.getCanceladaPor());
        dto.setCriadoEm(retirada.getCriadoEm());
        dto.setConfirmacoes(confirmacoes.stream().map(this::paraConfirmacaoDto).toList());
        return dto;
    }

    public RetiradaConfirmacaoDto paraConfirmacaoDto(RetiradaConfirmacao confirmacao) {
        return new RetiradaConfirmacaoDto(
                confirmacao.getCodigoAviso(),
                confirmacao.getSeveridade(),
                confirmacao.getMensagem(),
                confirmacao.getConfirmadoPor(),
                confirmacao.getConfirmadoEm(),
                confirmacao.getJustificativa()
        );
    }

    private String blankToNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
