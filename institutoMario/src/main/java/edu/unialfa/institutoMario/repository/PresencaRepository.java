package edu.unialfa.institutoMario.repository;

import edu.unialfa.institutoMario.model.Presenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;



@Repository
public interface PresencaRepository extends JpaRepository<Presenca, Long> {
    List<Presenca> findByChamadaId(Long chamadaId);

    // NOVO: Busca todas as presenças cruzando o ID da Turma e a Data da Chamada
    List<Presenca> findByChamadaTurmaIdAndChamadaData(Long turmaId, LocalDate data);

    // NOVO: Busca todo o histórico de presenças de um aluno específico
    List<Presenca> findByAlunoId(Long alunoId);
}