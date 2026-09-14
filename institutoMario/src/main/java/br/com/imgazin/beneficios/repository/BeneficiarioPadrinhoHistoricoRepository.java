package br.com.imgazin.beneficios.repository;

import br.com.imgazin.beneficios.domain.BeneficiarioPadrinhoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeneficiarioPadrinhoHistoricoRepository extends JpaRepository<BeneficiarioPadrinhoHistorico, Long> {

    Optional<BeneficiarioPadrinhoHistorico> findByBeneficiarioIdAndFimIsNull(Long beneficiarioId);

    List<BeneficiarioPadrinhoHistorico> findByVoluntarioIdOrderByInicioDesc(Long voluntarioId);

    List<BeneficiarioPadrinhoHistorico> findByBeneficiarioIdOrderByInicioDesc(Long beneficiarioId);
}
