package br.com.imgazin.beneficios.repository;

import br.com.imgazin.beneficios.domain.RetiradaCesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RetiradaCestaRepository extends JpaRepository<RetiradaCesta, Long>, JpaSpecificationExecutor<RetiradaCesta> {

    boolean existsByBeneficiarioIdAndMesReferenciaAndCanceladaFalse(Long beneficiarioId, LocalDate mesReferencia);

    Optional<RetiradaCesta> findByBeneficiarioIdAndMesReferenciaAndCanceladaFalse(Long beneficiarioId, LocalDate mesReferencia);

    List<RetiradaCesta> findByBeneficiarioIdOrderByMesReferenciaDesc(Long beneficiarioId);

    List<RetiradaCesta> findByBeneficiarioIdAndCanceladaFalseOrderByMesReferenciaDesc(Long beneficiarioId);

    List<RetiradaCesta> findByVoluntarioEntregaIdAndCanceladaFalseOrderByDataRetiradaDesc(Long voluntarioEntregaId);
}
