package br.com.imgazin.beneficios.repository;

import br.com.imgazin.beneficios.domain.BeneficiarioEndereco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiarioEnderecoRepository extends JpaRepository<BeneficiarioEndereco, Long> {

    Optional<BeneficiarioEndereco> findByBeneficiarioId(Long beneficiarioId);
}
