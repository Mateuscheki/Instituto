package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.model.Chamada;
import edu.unialfa.institutoMario.model.Presenca;
import edu.unialfa.institutoMario.model.Turma;
import edu.unialfa.institutoMario.repository.ChamadaRepository;
import edu.unialfa.institutoMario.repository.PresencaRepository;
import edu.unialfa.institutoMario.repository.TurmaRepository;
import edu.unialfa.institutoMario.service.ChamadaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/chamadas")
public class ChamadaController {

    @Autowired
    private ChamadaService chamadaService;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private ChamadaRepository chamadaRepository;

    @Autowired
    private PresencaRepository presencaRepository;

    // 1. Exibe a tela inicial para escolher a turma e a data
    @GetMapping("/nova")
    public String exibirFormularioNovaChamada(Model model) {
        model.addAttribute("turmas", turmaRepository.findAll());
        return "chamada/nova-chamada"; // Ajustado para buscar na pasta 'chamada'
    }

    // 2. Recebe os dados, cria a chamada e REDIRECIONA para a tela de lista de alunos
    @PostMapping("/salvar")
    public String salvarNovaChamada(@RequestParam("turmaId") Long turmaId,
                                    @RequestParam("data") LocalDate data) {

        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma inválida!"));

        Chamada novaChamada = chamadaService.iniciarChamada(turma, data);

        return "redirect:/chamadas/" + novaChamada.getId() + "/presencas";
    }

    // 3. NOVA ROTA: Exibe a lista de alunos (tabela) para o professor marcar as faltas
    @GetMapping("/{id}/presencas")
    public String exibirListaDeAlunos(@PathVariable("id") Long chamadaId, Model model) {
        Chamada chamada = chamadaRepository.findById(chamadaId)
                .orElseThrow(() -> new IllegalArgumentException("Chamada não encontrada!"));

        List<Presenca> listaPresencas = presencaRepository.findByChamadaId(chamadaId);

        model.addAttribute("chamada", chamada);
        model.addAttribute("listaPresencas", listaPresencas);

        return "chamada/lista-presenca"; // Ajustado para buscar na pasta 'chamada'
    }

    // 4. NOVA ROTA: Salva as alterações feitas nas caixas de seleção (checkbox)
    @PostMapping("/{id}/presencas/salvar")
    public String salvarPresencasNominais(
            @PathVariable("id") Long chamadaId,
            @RequestParam(value = "presencasPresentes", required = false) List<Long> presencasPresentes) {

        if (presencasPresentes == null) {
            presencasPresentes = new ArrayList<>();
        }

        List<Presenca> todasPresencas = presencaRepository.findByChamadaId(chamadaId);

        for (Presenca presenca : todasPresencas) {
            boolean estavaPresente = presencasPresentes.contains(presenca.getId());
            presenca.setPresente(estavaPresente);
            presencaRepository.save(presenca);
        }

        return "redirect:/chamadas/" + chamadaId + "/presencas?sucesso";
    }
}