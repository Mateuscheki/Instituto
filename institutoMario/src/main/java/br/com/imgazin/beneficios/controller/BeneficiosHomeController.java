package br.com.imgazin.beneficios.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Tela inicial do módulo Benefícios. Nesta etapa (fundação) só existem
 * atalhos "em breve" — nenhuma regra de negócio ainda. Controller fino, sem
 * @Transactional, sem lógica: só monta o Model e devolve a view (ver
 * convenções do CLAUDE.md do módulo).
 */
@Controller
@RequestMapping("/beneficios")
public class BeneficiosHomeController {

    @GetMapping
    public String home(Model model) {
        model.addAttribute("tituloPagina", "Início");
        return "beneficios/home";
    }
}
