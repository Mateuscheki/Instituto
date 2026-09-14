package br.com.imgazin.beneficios.controller;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.Voluntario;
import br.com.imgazin.beneficios.form.VincularPadrinhoForm;
import br.com.imgazin.beneficios.service.ApadrinhamentoService;
import br.com.imgazin.beneficios.service.BeneficiarioService;
import br.com.imgazin.beneficios.service.VoluntarioService;
import br.com.imgazin.beneficios.util.FormUtils;
import edu.unialfa.institutoMario.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Vínculo padrinho ↔ beneficiário. Fora do prefixo {@code /beneficios/beneficiarios}
 * e {@code /beneficios/voluntarios} de propósito — as rotas do CLAUDE.md misturam
 * os dois recursos ({@code /beneficios/beneficiarios/{id}/padrinho} e
 * {@code /beneficios/padrinhos/{voluntarioId}/apadrinhados}).
 */
@Controller
@RequiredArgsConstructor
public class ApadrinhamentoController {

    private final ApadrinhamentoService apadrinhamentoService;
    private final BeneficiarioService beneficiarioService;
    private final VoluntarioService voluntarioService;

    @PostMapping("/beneficios/beneficiarios/{id}/padrinho")
    public String vincular(@PathVariable Long id,
                            @RequestParam Long voluntarioId,
                            @RequestParam(required = false) List<String> avisosConfirmados,
                            @RequestParam(required = false) String justificativa,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        List<Aviso> avisos = apadrinhamentoService.avaliarVinculo(voluntarioId);
        List<String> confirmados = avisosConfirmados == null ? List.of() : avisosConfirmados;

        boolean todosConfirmados = avisos.stream().allMatch(a -> confirmados.contains(a.getCodigo()));
        boolean temJustificativa = justificativa != null && !justificativa.isBlank();

        if (!avisos.isEmpty() && (!todosConfirmados || !temJustificativa)) {
            VincularPadrinhoForm form = new VincularPadrinhoForm();
            form.setBeneficiarioId(id.toString());
            form.setVoluntarioId(voluntarioId.toString());

            Beneficiario beneficiario = beneficiarioService.buscarPorId(id);
            Voluntario voluntario = voluntarioService.buscarPorId(voluntarioId);

            model.addAttribute("tituloPagina", "Confirmar vínculo de padrinho");
            model.addAttribute("beneficiarioNome", beneficiario.getNome());
            model.addAttribute("voluntarioNome", voluntario.getNome());
            model.addAttribute("avisos", avisos);
            model.addAttribute("exigeJustificativa", true);
            model.addAttribute("justificativaDigitada", justificativa);
            model.addAttribute("camposOcultos", FormUtils.paraCamposOcultos(form));
            return "beneficios/beneficiarios/confirmar-padrinho";
        }

        apadrinhamentoService.vincular(id, voluntarioId, avisos, justificativa, usuarioLogado());
        redirectAttributes.addFlashAttribute("sucesso", "Padrinho/madrinha vinculado com sucesso.");
        return "redirect:/beneficios/beneficiarios/" + id;
    }

    @PostMapping("/beneficios/beneficiarios/{id}/padrinho/remover")
    public String remover(@PathVariable Long id, @RequestParam String motivo, RedirectAttributes redirectAttributes) {
        apadrinhamentoService.desvincular(id, motivo, usuarioLogado());
        redirectAttributes.addFlashAttribute("sucesso", "Padrinho/madrinha desvinculado com sucesso.");
        return "redirect:/beneficios/beneficiarios/" + id;
    }

    @GetMapping("/beneficios/padrinhos/{voluntarioId}/apadrinhados")
    public String apadrinhados(@PathVariable Long voluntarioId, Model model) {
        Voluntario voluntario = voluntarioService.buscarPorId(voluntarioId);
        model.addAttribute("tituloPagina", "Apadrinhados");
        model.addAttribute("voluntarioId", voluntarioId);
        model.addAttribute("voluntarioNome", voluntario.getNome());
        model.addAttribute("apadrinhados", apadrinhamentoService.listarApadrinhados(voluntarioId));
        return "beneficios/padrinhos/apadrinhados";
    }

    private String usuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario.getNome();
        }
        return "sistema";
    }
}
