package br.com.imgazin.beneficios.repository;

import br.com.imgazin.beneficios.domain.RetiradaCesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RetiradaCestaRepository extends JpaRepository<RetiradaCesta, Long>, JpaSpecificationExecutor<RetiradaCesta> {

    /**
     * Espelha em Java a constraint garantida no banco pela coluna gerada
     * {@code mes_referencia_ativo} (ver migration V1): existe alguma retirada
     * NÃO cancelada desse beneficiário nesse mês?
     */
    boolean existsByBeneficiarioIdAndMesReferenciaAndCanceladaFalse(Long beneficiarioId, LocalDate mesReferencia);

    /** Mesma checagem, mas devolvendo o registro (usado pelo aviso JA_RETIROU_NO_MES e pelo painel de elegibilidade). */
    Optional<RetiradaCesta> findByBeneficiarioIdAndMesReferenciaAndCanceladaFalse(Long beneficiarioId, LocalDate mesReferencia);

    List<RetiradaCesta> findByBeneficiarioIdOrderByMesReferenciaDesc(Long beneficiarioId);

    /** Só as retiradas válidas (não canceladas) — usado no resumo do histórico do beneficiário. */
    List<RetiradaCesta> findByBeneficiarioIdAndCanceladaFalseOrderByMesReferenciaDesc(Long beneficiarioId);

    /** Entregas feitas por um voluntário (tela de atuação, Etapa 4). */
    List<RetiradaCesta> findByVoluntarioEntregaIdAndCanceladaFalseOrderByDataRetiradaDesc(Long voluntarioEntregaId);
}
