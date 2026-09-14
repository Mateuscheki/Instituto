package br.com.imgazin.beneficios.repository;

import br.com.imgazin.beneficios.domain.RetiradaConfirmacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RetiradaConfirmacaoRepository extends JpaRepository<RetiradaConfirmacao, Long> {

    List<RetiradaConfirmacao> findByRetiradaId(Long retiradaId);
}
