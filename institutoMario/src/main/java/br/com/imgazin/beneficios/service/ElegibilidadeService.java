package br.com.imgazin.beneficios.service;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.aviso.ContextoAvaliacaoRetirada;
import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.RetiradaCesta;
import br.com.imgazin.beneficios.domain.SituacaoElegibilidade;
import br.com.imgazin.beneficios.domain.StatusBeneficiario;
import br.com.imgazin.beneficios.dto.ElegibilidadeDto;
import br.com.imgazin.beneficios.form.RetiradaForm;
import br.com.imgazin.beneficios.mapper.RetiradaMapper;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import br.com.imgazin.beneficios.repository.RetiradaCestaRepository;
import br.com.imgazin.beneficios.util.CpfUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Monta o painel de elegibilidade exibido antes do formulário de retirada
 * (ver {@code retiradas/registrar.html}). "Avisos preliminares" são uma
 * prévia rodando o motor de avisos só com o que já se sabe (CPF + mês) —
 * ficam mais completos quando o formulário inteiro é submetido.
 */
@Service
@RequiredArgsConstructor
public class ElegibilidadeService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final RetiradaCestaRepository retiradaCestaRepository;
    private final ValidacaoRetiradaService validacaoRetiradaService;
    private final RetiradaMapper mapper;

    public Optional<ElegibilidadeDto> avaliar(String cpf, String mesReferenciaTexto) {
        String cpfNormalizado = CpfUtils.normalizar(cpf);
        if (cpfNormalizado == null || cpfNormalizado.length() != 11) {
            return Optional.empty();
        }

        LocalDate mes = parsearMes(mesReferenciaTexto);
        Optional<Beneficiario> beneficiarioOpt = beneficiarioRepository.findByCpf(cpfNormalizado);

        if (beneficiarioOpt.isEmpty()) {
            ElegibilidadeDto dto = new ElegibilidadeDto();
            dto.setCpf(cpfNormalizado);
            dto.setSituacao(SituacaoElegibilidade.NAO_CADASTRADO);
            dto.setMesReferencia(mes);
            dto.setAvisosPreliminares(List.of());
            return Optional.of(dto);
        }

        Beneficiario beneficiario = beneficiarioOpt.get();

        RetiradaCesta ultimaValida = retiradaCestaRepository
                .findByBeneficiarioIdOrderByMesReferenciaDesc(beneficiario.getId()).stream()
                .filter(r -> !r.isCancelada())
                .findFirst()
                .orElse(null);

        SituacaoElegibilidade situacao = calcularSituacao(beneficiario, mes);
        List<Aviso> avisosPreliminares = calcularAvisosPreliminares(beneficiario, cpfNormalizado, mesReferenciaTexto);

        return Optional.of(mapper.paraElegibilidade(beneficiario, mes, situacao, ultimaValida, avisosPreliminares));
    }

    private List<Aviso> calcularAvisosPreliminares(Beneficiario beneficiario, String cpfNormalizado, String mesReferenciaTexto) {
        RetiradaForm formPrevia = new RetiradaForm();
        formPrevia.setCpfBeneficiario(cpfNormalizado);
        formPrevia.setMesReferencia(mesReferenciaTexto);
        return validacaoRetiradaService.avaliar(new ContextoAvaliacaoRetirada(formPrevia, beneficiario));
    }

    private SituacaoElegibilidade calcularSituacao(Beneficiario beneficiario, LocalDate mes) {
        if (!beneficiario.isAtivo() || beneficiario.getStatus() == StatusBeneficiario.INATIVO) {
            return SituacaoElegibilidade.INATIVO;
        }
        if (beneficiario.getStatus() == StatusBeneficiario.SUSPENSO) {
            return SituacaoElegibilidade.SUSPENSO;
        }
        if (beneficiario.getPrazoFinalBeneficio() != null && beneficiario.getPrazoFinalBeneficio().isBefore(LocalDate.now())) {
            return SituacaoElegibilidade.PRAZO_ENCERRADO;
        }
        if (mes != null && retiradaCestaRepository.existsByBeneficiarioIdAndMesReferenciaAndCanceladaFalse(beneficiario.getId(), mes)) {
            return SituacaoElegibilidade.JA_RETIROU;
        }
        return SituacaoElegibilidade.ELEGIVEL;
    }

    private LocalDate parsearMes(String valor) {
        if (valor == null || valor.isBlank()) {
            return LocalDate.now().withDayOfMonth(1);
        }
        try {
            return YearMonth.parse(valor).atDay(1);
        } catch (Exception e) {
            return LocalDate.now().withDayOfMonth(1);
        }
    }
}
