package br.com.imgazin.beneficios.service;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.domain.AcaoAuditoria;
import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.BeneficiarioPadrinhoHistorico;
import br.com.imgazin.beneficios.domain.Voluntario;
import br.com.imgazin.beneficios.dto.ApadrinhadoResumoDto;
import br.com.imgazin.beneficios.repository.BeneficiarioPadrinhoHistoricoRepository;
import br.com.imgazin.beneficios.repository.BeneficiarioRepository;
import br.com.imgazin.beneficios.repository.RetiradaCestaRepository;
import br.com.imgazin.beneficios.repository.VoluntarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Vínculo padrinho ↔ beneficiário. Um beneficiário tem um padrinho por vez
 * (campo {@code beneficiario.padrinho}); o histórico completo (quem foi
 * padrinho, de quando a quando) vive em {@code beneficiario_padrinho_historico}
 * e NUNCA é apagado — trocar de padrinho só encerra o período atual (fim =
 * hoje) e abre um novo, então relatórios antigos continuam corretos.
 */
@Service
@RequiredArgsConstructor
public class ApadrinhamentoService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final VoluntarioRepository voluntarioRepository;
    private final BeneficiarioPadrinhoHistoricoRepository historicoRepository;
    private final RetiradaCestaRepository retiradaCestaRepository;
    private final BeneficiosAuditoriaService auditoriaService;

    /** VOLUNTARIO_NAO_E_PADRINHO (ALTA): só quem tem is_padrinho=true e está ativo pode ser vinculado. */
    public List<Aviso> avaliarVinculo(Long voluntarioId) {
        Voluntario voluntario = voluntarioRepository.findById(voluntarioId)
                .orElseThrow(() -> new EntityNotFoundException("Voluntário não encontrado: " + voluntarioId));

        if (voluntario.isPadrinho() && voluntario.isAtivo()) {
            return List.of();
        }

        return List.of(Aviso.alta("VOLUNTARIO_NAO_E_PADRINHO",
                "Este voluntário não está marcado como padrinho/madrinha ativo. Confirmando, ele passa a ser.",
                voluntarioId));
    }

    @Transactional
    public void vincular(Long beneficiarioId, Long voluntarioId, List<Aviso> avisosConfirmados,
                          String justificativa, String usuarioLogado) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado: " + beneficiarioId));
        Voluntario voluntario = voluntarioRepository.findById(voluntarioId)
                .orElseThrow(() -> new EntityNotFoundException("Voluntário não encontrado: " + voluntarioId));

        boolean confirmouNaoEhPadrinho = avisosConfirmados != null && avisosConfirmados.stream()
                .anyMatch(aviso -> "VOLUNTARIO_NAO_E_PADRINHO".equals(aviso.getCodigo()));
        if (confirmouNaoEhPadrinho && !voluntario.isPadrinho()) {
            voluntario.setPadrinho(true);
            voluntarioRepository.save(voluntario);
        }

        encerrarVinculoAtualSeExistir(beneficiario, "Substituído por novo vínculo.");

        BeneficiarioPadrinhoHistorico novoVinculo = new BeneficiarioPadrinhoHistorico();
        novoVinculo.setBeneficiario(beneficiario);
        novoVinculo.setVoluntario(voluntario);
        novoVinculo.setInicio(LocalDate.now());
        historicoRepository.save(novoVinculo);

        beneficiario.setPadrinho(voluntario);
        beneficiarioRepository.save(beneficiario);

        String detalhes = "Padrinho/madrinha vinculado: " + voluntario.getNome() + " (ID " + voluntario.getId() + ")"
                + (justificativa != null && !justificativa.isBlank() ? " | Justificativa: " + justificativa : "");
        auditoriaService.registrar(AcaoAuditoria.EDITAR, "Beneficiario", beneficiarioId, detalhes);
    }

    @Transactional
    public void desvincular(Long beneficiarioId, String motivo, String usuarioLogado) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado: " + beneficiarioId));

        encerrarVinculoAtualSeExistir(beneficiario, motivo);

        beneficiario.setPadrinho(null);
        beneficiarioRepository.save(beneficiario);

        auditoriaService.registrar(AcaoAuditoria.EDITAR, "Beneficiario", beneficiarioId,
                "Padrinho/madrinha desvinculado. Motivo: " + motivo);
    }

    private void encerrarVinculoAtualSeExistir(Beneficiario beneficiario, String motivoFim) {
        historicoRepository.findByBeneficiarioIdAndFimIsNull(beneficiario.getId()).ifPresent(vinculoAtual -> {
            vinculoAtual.setFim(LocalDate.now());
            vinculoAtual.setMotivoFim(motivoFim);
            historicoRepository.save(vinculoAtual);
        });
    }

    public List<ApadrinhadoResumoDto> listarApadrinhados(Long voluntarioId) {
        LocalDate mesAtual = LocalDate.now().withDayOfMonth(1);
        return beneficiarioRepository.findByPadrinhoId(voluntarioId).stream()
                .map(beneficiario -> new ApadrinhadoResumoDto(
                        beneficiario.getId(),
                        beneficiario.getNome(),
                        beneficiario.getCpf(),
                        retiradaCestaRepository.existsByBeneficiarioIdAndMesReferenciaAndCanceladaFalse(beneficiario.getId(), mesAtual),
                        beneficiario.isAtivo()
                ))
                .toList();
    }
}
