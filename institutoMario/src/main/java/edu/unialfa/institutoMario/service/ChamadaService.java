package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.model.Aluno;
import edu.unialfa.institutoMario.model.Chamada;
import edu.unialfa.institutoMario.model.Presenca;
import edu.unialfa.institutoMario.model.Turma;
import edu.unialfa.institutoMario.repository.AlunoRepository;
import edu.unialfa.institutoMario.repository.ChamadaRepository;
import edu.unialfa.institutoMario.repository.PresencaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;


@Service
public class ChamadaService {

    @Autowired
    private ChamadaRepository chamadaRepository;

    @Autowired
    private PresencaRepository presencaRepository;

    @Autowired
    private AlunoRepository alunoRepository; // Assumindo que você já tem este repositório

    @Transactional
    public Chamada iniciarChamada(Turma turma, LocalDate data) {
        // 1. Cria e salva o registro do dia da chamada
        Chamada novaChamada = new Chamada(data, turma);
        novaChamada = chamadaRepository.save(novaChamada);

        // 2. Busca todos os alunos que pertencem a essa turma específica
        List<Aluno> alunosDaTurma = alunoRepository.findByTurmas(turma);

        // 3. Para cada aluno, cria um registro de presença vinculado a esta chamada
        for (Aluno aluno : alunosDaTurma) {
            // Inicializamos todos como 'presente' (true) para facilitar na tela,
            // o professor só desmarca quem faltou.
            Presenca presenca = new Presenca(novaChamada, aluno, true);
            presencaRepository.save(presenca);
        }

        return novaChamada;
    }
}