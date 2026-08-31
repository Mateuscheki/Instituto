package edu.unialfa.institutoMario.repository;

import edu.unialfa.institutoMario.model.Chamada;
import edu.unialfa.institutoMario.model.Presenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChamadaRepository extends JpaRepository<Chamada, Long> {
    // Você pode adicionar buscas customizadas aqui depois, como buscar chamadas por data
}

