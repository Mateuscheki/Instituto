package br.com.imgazin.beneficios.controller;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.domain.Voluntario;
import br.com.imgazin.beneficios.dto.VoluntarioListaItemDto;
import br.com.imgazin.beneficios.exception.CpfDuplicadoException;
import br.com.imgazin.beneficios.form.VoluntarioFiltro;
import br.com.imgazin.beneficios.form.VoluntarioForm;
import br.com.imgazin.beneficios.mapper.VoluntarioMapper;
import br.com.imgazin.beneficios.service.ValidacaoVoluntarioService;
import br.com.imgazin.beneficios.service.VoluntarioService;
import br.com.imgazin.beneficios.util.FormUtils;
import br.com.imgazin.beneficios.validation.VoluntarioFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/beneficios/voluntarios")
@RequiredArgsConstructor
public class VoluntarioController {

    private final VoluntarioService voluntarioService;
    private final ValidacaoVoluntarioService validacaoVoluntarioService;
    private final VoluntarioFormValidator voluntarioFormValidator;
    private final VoluntarioMapper mapper;

    @InitBinder("voluntarioForm")
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Long.class, new CustomNumberEditor(Long.class, true));
    }

    @GetMapping
    public String listar(VoluntarioFiltro filtro,
                          @PageableDefault(size = 20, sort = "nome") Pageable pageable,
                          Model model) {
        Page<VoluntarioListaItemDto> pagina = voluntarioService.listar(filtro, pageable);
        model.addAttribute("tituloPagina", "Voluntários");
        model.addAttribute("pagina", pagina);
        model.addAttribute("filtro", filtro);
        return "voluntarios/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("tituloPagina", "Novo voluntário");
        model.addAttribute("voluntarioForm", new VoluntarioForm());
        return "voluntarios/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Voluntario voluntario = voluntarioService.buscarPorId(id);
        model.addAttribute("tituloPagina", "Editar voluntário");
        model.addAttribute("voluntarioForm", mapper.paraForm(voluntario));
        return "voluntarios/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("voluntarioForm") VoluntarioForm form,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        voluntarioFormValidator.validate(form, bindingResult);
        verificarCpfDuplicadoSeNecessario(form, bindingResult, model);

        if (bindingResult.hasErrors()) {
            model.addAttribute("tituloPagina", form.getId() == null ? "Novo voluntário" : "Editar voluntário");
            return "voluntarios/formulario";
        }

        List<Aviso> avisos = validacaoVoluntarioService.avaliar(form);
        if (!avisos.isEmpty()) {
            prepararModelDaConfirmacao(model, form, avisos);
            return "voluntarios/confirmacao";
        }

        Voluntario salvo = voluntarioService.salvar(form, List.of(), null);
        redirectAttributes.addFlashAttribute("sucesso", "Voluntário salvo com sucesso.");
        return "redirect:/beneficios/voluntarios/" + salvo.getId();
    }

    @PostMapping("/confirmar")
    public String confirmar(@ModelAttribute("voluntarioForm") VoluntarioForm form,
                             BindingResult bindingResult,
                             @RequestParam(name = "avisosConfirmados", required = false) List<String> codigosConfirmados,
                             @RequestParam(required = false) String justificativa,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        voluntarioFormValidator.validate(form, bindingResult);
        verificarCpfDuplicadoSeNecessario(form, bindingResult, model);

        if (bindingResult.hasErrors()) {
            model.addAttribute("tituloPagina", form.getId() == null ? "Novo voluntário" : "Editar voluntário");
            return "voluntarios/formulario";
        }

        List<Aviso> avisosAtuais = validacaoVoluntarioService.avaliar(form);
        List<String> confirmados = codigosConfirmados == null ? List.of() : codigosConfirmados;

        List<Aviso> altasNaoConfirmados = avisosAtuais.stream()
                .filter(Aviso::isExigeJustificativa)
                .filter(aviso -> !confirmados.contains(aviso.getCodigo()))
                .toList();
        boolean exigeJustificativa = avisosAtuais.stream().anyMatch(Aviso::isExigeJustificativa);
        boolean faltaJustificativa = exigeJustificativa && (justificativa == null || justificativa.isBlank());

        if (!altasNaoConfirmados.isEmpty() || faltaJustificativa) {
            prepararModelDaConfirmacao(model, form, avisosAtuais);
            model.addAttribute("avisosDestacados", altasNaoConfirmados);
            model.addAttribute("avisosJaConfirmados", confirmados);
            model.addAttribute("justificativaDigitada", justificativa);
            model.addAttribute("erroConfirmacao", faltaJustificativa
                    ? "Informe a justificativa antes de confirmar."
                    : "Confirme todos os avisos de alta severidade antes de prosseguir.");
            return "voluntarios/confirmacao";
        }

        List<Aviso> avisosParaPersistir = avisosAtuais.stream()
                .filter(aviso -> !aviso.isExigeJustificativa() || confirmados.contains(aviso.getCodigo()))
                .toList();

        Voluntario salvo = voluntarioService.salvar(form, avisosParaPersistir, justificativa);
        redirectAttributes.addFlashAttribute("sucesso", "Voluntário salvo com sucesso — avisos confirmados registrados.");
        return "redirect:/beneficios/voluntarios/" + salvo.getId();
    }

    @PostMapping("/voltar")
    public String voltar(@ModelAttribute("voluntarioForm") VoluntarioForm form, Model model) {
        model.addAttribute("tituloPagina", form.getId() == null ? "Novo voluntário" : "Editar voluntário");
        return "voluntarios/formulario";
    }

    @GetMapping("/{id}")
    public String atuacao(@PathVariable Long id, Model model) {
        model.addAttribute("tituloPagina", "Atuação do voluntário");
        model.addAttribute("atuacao", voluntarioService.montarAtuacao(id));
        return "voluntarios/atuacao";
    }

    @PostMapping("/{id}/inativar")
    public String inativar(@PathVariable Long id, @RequestParam String motivo, RedirectAttributes redirectAttributes) {
        voluntarioService.inativar(id, motivo);
        redirectAttributes.addFlashAttribute("sucesso", "Voluntário inativado com sucesso.");
        return "redirect:/beneficios/voluntarios/" + id;
    }

    private void verificarCpfDuplicadoSeNecessario(VoluntarioForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasFieldErrors("cpf")) {
            return;
        }
        try {
            voluntarioService.verificarCpfDuplicado(form);
        } catch (CpfDuplicadoException e) {
            bindingResult.rejectValue("cpf", "cpf.duplicado", "Já existe um voluntário cadastrado com este CPF.");
            model.addAttribute("cpfDuplicadoId", e.getIdExistente());
        }
    }

    private void prepararModelDaConfirmacao(Model model, VoluntarioForm form, List<Aviso> avisos) {
        model.addAttribute("tituloPagina", "Confirmar cadastro");
        model.addAttribute("voluntarioForm", form);
        model.addAttribute("avisos", avisos);
        model.addAttribute("exigeJustificativa", avisos.stream().anyMatch(Aviso::isExigeJustificativa));
        model.addAttribute("camposOcultos", FormUtils.paraCamposOcultos(form));
    }
}
