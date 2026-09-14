package br.com.imgazin.beneficios.repository;

import br.com.imgazin.beneficios.domain.BeneficiosAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiosAuditoriaRepository extends JpaRepository<BeneficiosAuditoria, Long> {

    List<BeneficiosAuditoria> findByEntidadeAndEntidadeIdOrderByDataHoraDesc(String entidade, Long entidadeId);
}
