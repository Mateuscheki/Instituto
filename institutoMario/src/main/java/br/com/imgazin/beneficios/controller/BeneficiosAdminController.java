package br.com.imgazin.beneficios.controller;

import br.com.imgazin.beneficios.service.RecalculoStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Ações administrativas do módulo. Todo {@code /beneficios/**} já exige
 * ROLE_ADM (ver SecurityConfig) — não precisa de checagem extra aqui.
 */
@Controller
@RequestMapping("/beneficios/admin")
@RequiredArgsConstructor
public class BeneficiosAdminController {

    private final RecalculoStatusService recalculoStatusService;

    @PostMapping("/recalcular-status")
    public String recalcularStatus(RedirectAttributes redirectAttributes) {
        RecalculoStatusService.ResultadoRecalculo resultado = recalculoStatusService.executar();
        redirectAttributes.addFlashAttribute("sucesso",
                "Recálculo executado: " + resultado.voltaramAptos() + " beneficiário(s) voltaram a apto, "
                        + resultado.prazosEncerrados() + " tiveram o prazo encerrado.");
        return "redirect:/beneficios";
    }
}
