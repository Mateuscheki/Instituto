package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.audit.LogAuditoriaService;
import edu.unialfa.institutoMario.audit.TipoAcao;
import edu.unialfa.institutoMario.dto.ChamadaResumoDTO;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @Autowired
    private LogAuditoriaService logAuditoriaService;

    // 0. Lista as chamadas já feitas, para o administrador poder reabrir e alterar
    @GetMapping
    public String listar(Model model) {
        List<ChamadaResumoDTO> resumos = chamadaService.listarTodas().stream()
                .map(chamada -> {
                    List<Presenca> presencas = presencaRepository.findByChamadaId(chamada.getId());
                    long presentes = presencas.stream()
                            .filter(p -> Boolean.TRUE.equals(p.getPresente()))
                            .count();
                    return new ChamadaResumoDTO(chamada.getId(), chamada.getTurma().getNome(),
                            chamada.getData(), presencas.size(), presentes);
                })
                .toList();

        model.addAttribute("chamadas", resumos);
        return "chamada/lista";
    }

    // 1. Exibe a tela inicial para escolher a turma e a data
    @GetMapping("/nova")
    public String exibirFormularioNovaChamada(Model model) {
        model.addAttribute("turmas", turmaRepository.findAll());
        return "chamada/nova-chamada"; // Ajustado para buscar na pasta 'chamada'
    }

    // 2. Recebe os dados, cria a chamada (ou reabre a existente, se já houver uma
    //    chamada dessa turma nessa data) e REDIRECIONA para a tela de lista de alunos
    @PostMapping("/salvar")
    public String salvarNovaChamada(@RequestParam("turmaId") Long turmaId,
                                    @RequestParam("data") LocalDate data,
                                    RedirectAttributes redirectAttributes) {

        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma inválida!"));

        boolean jaExistiaChamada = chamadaService.buscarPorTurmaEData(turmaId, data).isPresent();

        Chamada chamada = chamadaService.iniciarChamada(turma, data);

        if (jaExistiaChamada) {
            redirectAttributes.addFlashAttribute("mensagemInfo",
                    "Já existe uma chamada de " + turma.getNome() + " nesta data. Você está editando a chamada existente.");
        }

        return "redirect:/chamadas/" + chamada.getId() + "/presencas";
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

        logAuditoriaService.registrar(TipoAcao.ATUALIZACAO, "Chamada", chamadaId,
                "Presenças atualizadas para a Chamada (ID " + chamadaId + ")");

        return "redirect:/chamadas/" + chamadaId + "/presencas?sucesso";
    }
}