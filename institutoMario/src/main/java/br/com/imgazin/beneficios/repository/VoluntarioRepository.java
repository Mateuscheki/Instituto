package br.com.imgazin.beneficios.repository;

import br.com.imgazin.beneficios.domain.Voluntario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface VoluntarioRepository extends JpaRepository<Voluntario, Long>, JpaSpecificationExecutor<Voluntario> {

    Optional<Voluntario> findByCpf(String cpf);

    List<Voluntario> findByAtivoTrue();

    List<Voluntario> findByPadrinhoTrueAndAtivoTrue();
}
