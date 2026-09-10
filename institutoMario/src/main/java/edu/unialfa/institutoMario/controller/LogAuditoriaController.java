package edu.unialfa.institutoMario.controller;

import edu.unialfa.institutoMario.audit.LogAuditoriaService;
import edu.unialfa.institutoMario.audit.TipoAcao;
import edu.unialfa.institutoMario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tela de consulta ao log de auditoria, restrita a ADMIN (ver SecurityConfig).
 */
@Controller
@RequestMapping("/logs-auditoria")
@RequiredArgsConstructor
public class LogAuditoriaController {

    private final LogAuditoriaService logAuditoriaService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) TipoAcao acao,
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = dataFim != null ? dataFim.atTime(23, 59, 59) : null;

        PageRequest pageRequest = PageRequest.of(pagina, 30, Sort.by(Sort.Direction.DESC, "dataHora"));
        Page<edu.unialfa.institutoMario.audit.LogAuditoria> logs =
                logAuditoriaService.buscar(usuarioId, acao, entidade, inicio, fim, pageRequest);

        model.addAttribute("logs", logs);
        model.addAttribute("tiposAcao", TipoAcao.values());
        model.addAttribute("entidades", logAuditoriaService.listarEntidadesDistintas());

        model.addAttribute("usuarioId", usuarioId);
        model.addAttribute("acao", acao);
        model.addAttribute("entidade", entidade);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);

        return "logs-auditoria/lista";
    }

    @org.springframework.web.bind.annotation.ModelAttribute("usuarioLogado")
    public Object getUsuarioLogado() {
        return usuarioService.getUsuarioLogado();
    }
}
