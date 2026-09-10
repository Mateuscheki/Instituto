package edu.unialfa.institutoMario.repository;

import edu.unialfa.institutoMario.model.Chamada;
import edu.unialfa.institutoMario.model.Presenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChamadaRepository extends JpaRepository<Chamada, Long> {

    /** Usado para impedir duas chamadas da mesma turma no mesmo dia. */
    Optional<Chamada> findByTurmaIdAndData(Long turmaId, LocalDate data);

    List<Chamada> findAllByOrderByDataDesc();
}

