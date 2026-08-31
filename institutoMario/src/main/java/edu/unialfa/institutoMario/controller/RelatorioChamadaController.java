package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Presenca;
import edu.unialfa.institutoMario.repository.AlunoRepository;
import edu.unialfa.institutoMario.repository.PresencaRepository;
import edu.unialfa.institutoMario.repository.TurmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/relatorios/chamadas")
public class RelatorioChamadaController {

    @Autowired
    private PresencaRepository presencaRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    /**
     * Rota para o Relatório por Turma e Dia
     * URL: /relatorios/chamadas/turma
     */
    @GetMapping("/turma")
    public String relatorioPorTurma(
            @RequestParam(required = false) Long turmaId,
            @RequestParam(required = false) LocalDate data,
            Model model) {

        // Envia a lista de turmas para preencher o dropdown (Select)
        model.addAttribute("turmas", turmaRepository.findAll());

        // Se o usuário preencheu o filtro e clicou em buscar, executamos a consulta
        if (turmaId != null && data != null) {
            List<Presenca> resultados = presencaRepository.findByChamadaTurmaIdAndChamadaData(turmaId, data);
            model.addAttribute("resultados", resultados);
            model.addAttribute("dataFiltro", data);

            // Opcional: Conta os presentes e ausentes para um resumo
            long presentes = resultados.stream().filter(Presenca::getPresente).count();
            model.addAttribute("totalPresentes", presentes);
            model.addAttribute("totalAusentes", resultados.size() - presentes);
        }

        return "relatorios/relatorio-chamada-turma";
    }

    /**
     * Rota para o Relatório por Aluno
     * URL: /relatorios/chamadas/aluno
     */
    @GetMapping("/aluno")
    public String relatorioPorAluno(
            @RequestParam(required = false) Long alunoId,
            Model model) {

        // Envia a lista de alunos para o filtro
        model.addAttribute("alunos", alunoRepository.findAll());

        // Se um aluno foi selecionado, busca o histórico dele
        if (alunoId != null) {
            List<Presenca> resultados = presencaRepository.findByAlunoId(alunoId);
            model.addAttribute("resultados", resultados);

            long presentes = resultados.stream().filter(Presenca::getPresente).count();
            model.addAttribute("totalPresentes", presentes);
            model.addAttribute("totalFaltas", resultados.size() - presentes);
        }

        return "relatorios/relatorio-chamada-aluno";
    }
}