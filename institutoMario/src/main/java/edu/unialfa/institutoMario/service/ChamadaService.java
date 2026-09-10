package edu.unialfa.institutoMario.service;

import edu.unialfa.institutoMario.audit.LogAuditoriaService;
import edu.unialfa.institutoMario.audit.TipoAcao;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class ChamadaService {

    @Autowired
    private ChamadaRepository chamadaRepository;

    @Autowired
    private PresencaRepository presencaRepository;

    @Autowired
    private AlunoRepository alunoRepository; // Assumindo que você já tem este repositório

    @Autowired
    private LogAuditoriaService logAuditoriaService;

    public Optional<Chamada> buscarPorTurmaEData(Long turmaId, LocalDate data) {
        return chamadaRepository.findByTurmaIdAndData(turmaId, data);
    }

    public List<Chamada> listarTodas() {
        return chamadaRepository.findAllByOrderByDataDesc();
    }

    /**
     * Inicia a chamada de uma turma em uma data. Já existindo uma chamada para essa
     * mesma turma+data (restrição também garantida no banco), reaproveita o registro
     * existente em vez de duplicar — apenas garante que alunos matriculados depois
     * também ganhem uma linha de presença. Isso é o que permite ao administrador
     * reabrir e alterar uma chamada já feita, em vez de criar chamadas repetidas.
     */
    @Transactional
    public Chamada iniciarChamada(Turma turma, LocalDate data) {
        Optional<Chamada> chamadaExistente = chamadaRepository.findByTurmaIdAndData(turma.getId(), data);
        if (chamadaExistente.isPresent()) {
            Chamada chamada = chamadaExistente.get();
            sincronizarPresencasComTurma(chamada, turma);
            return chamada;
        }

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

        logAuditoriaService.registrar(TipoAcao.CRIACAO, "Chamada", novaChamada.getId(),
                "Chamada criado(a) (ID " + novaChamada.getId() + ") para a Turma (ID " + turma.getId() + ")");

        return novaChamada;
    }

    /** Garante uma linha de presença para cada aluno atualmente matriculado na turma. */
    private void sincronizarPresencasComTurma(Chamada chamada, Turma turma) {
        List<Aluno> alunosDaTurma = alunoRepository.findByTurmas(turma);
        Set<Long> alunosComPresenca = presencaRepository.findByChamadaId(chamada.getId()).stream()
                .map(p -> p.getAluno().getId())
                .collect(Collectors.toSet());

        for (Aluno aluno : alunosDaTurma) {
            if (!alunosComPresenca.contains(aluno.getId())) {
                presencaRepository.save(new Presenca(chamada, aluno, true));
            }
        }
    }
}