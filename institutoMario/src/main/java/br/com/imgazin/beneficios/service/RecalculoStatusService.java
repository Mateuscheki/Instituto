package br.com.imgazin.beneficios.service;

import br.com.imgazin.beneficios.domain.AcaoAuditoria;
import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.StatusBeneficiario;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecalculoStatusService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final BeneficiosAuditoriaService auditoriaService;

    @Scheduled(cron = "0 5 0 1 * *", zone = "America/Sao_Paulo")
    public void executarAgendado() {
        executar();
    }

    @Transactional
    public ResultadoRecalculo executar() {
        LocalDate hoje = LocalDate.now();

        List<Beneficiario> retomamAptos = beneficiarioRepository.findByStatus(StatusBeneficiario.RETIRADO_MES_ATUAL);
        retomamAptos.forEach(b -> b.setStatus(StatusBeneficiario.APTO_MES_ATUAL));
        beneficiarioRepository.saveAll(retomamAptos);

        List<Beneficiario> prazosVencidos = beneficiarioRepository
                .findByAtivoTrueAndPrazoFinalBeneficioBeforeAndStatusNot(hoje, StatusBeneficiario.PRAZO_ENCERRADO);
        prazosVencidos.forEach(b -> b.setStatus(StatusBeneficiario.PRAZO_ENCERRADO));
        beneficiarioRepository.saveAll(prazosVencidos);

        ResultadoRecalculo resultado = new ResultadoRecalculo(retomamAptos.size(), prazosVencidos.size());

        log.info("Recálculo de status do módulo Benefícios: {} beneficiário(s) voltaram a APTO_MES_ATUAL, {} tiveram o prazo encerrado.",
                resultado.voltaramAptos(), resultado.prazosEncerrados());

        auditoriaService.registrar(AcaoAuditoria.EDITAR, "Beneficiario", null,
                "Recálculo de status (virada de mês): " + resultado.voltaramAptos() + " voltaram a apto, "
                        + resultado.prazosEncerrados() + " tiveram prazo encerrado.");

        return resultado;
    }

    public record ResultadoRecalculo(int voltaramAptos, int prazosEncerrados) {
    }
}
