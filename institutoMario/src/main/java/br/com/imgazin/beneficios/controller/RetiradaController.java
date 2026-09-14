package br.com.imgazin.beneficios.controller;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.aviso.ContextoAvaliacaoRetirada;
import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.RetiradaCesta;
import br.com.imgazin.beneficios.domain.TipoRetirante;
import br.com.imgazin.beneficios.dto.ElegibilidadeDto;
import br.com.imgazin.beneficios.dto.RetiradaListaItemDto;
import br.com.imgazin.beneficios.exception.RetiradaConcorrenteException;
import br.com.imgazin.beneficios.form.RetiradaFiltro;
import br.com.imgazin.beneficios.form.RetiradaForm;
import br.com.imgazin.beneficios.repository.VoluntarioRepository;
import br.com.imgazin.beneficios.service.BeneficiarioService;
import br.com.imgazin.beneficios.service.ElegibilidadeService;
import br.com.imgazin.beneficios.service.RetiradaService;
import br.com.imgazin.beneficios.service.ValidacaoRetiradaService;
import br.com.imgazin.beneficios.util.FormUtils;
import br.com.imgazin.beneficios.validation.RetiradaFormValidator;
import edu.unialfa.institutoMario.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller fino (ver CLAUDE.md): recebe form, chama service, popula
 * Model, devolve a view. Reusa o mesmo padrão de tela de confirmação de
 * avisos da Etapa 2 (hidden fields via FormUtils + fragments/campos.html).
 */
@Controller
@RequestMapping("/beneficios/retiradas")
@RequiredArgsConstructor
public class RetiradaController {

    private static final DateTimeFormatter DATA_HORA_FORM = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final RetiradaService retiradaService;
    private final ElegibilidadeService elegibilidadeService;
    private final ValidacaoRetiradaService validacaoRetiradaService;
    private final RetiradaFormValidator retiradaFormValidator;
    private final BeneficiarioService beneficiarioService;
    private final VoluntarioRepository voluntarioRepository;

    @InitBinder("retiradaForm")
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Long.class, new CustomNumberEditor(Long.class, true));
    }

    @GetMapping("/registrar")
    public String registrar(@RequestParam(required = false) String cpf,
                             @RequestParam(required = false) String mes,
                             Model model) {
        model.addAttribute("tituloPagina", "Registrar retirada");
        model.addAttribute("cpfPesquisado", cpf);

        if (cpf == null || cpf.isBlank()) {
            return "retiradas/registrar";
        }

        String mesEfetivo = (mes == null || mes.isBlank()) ? YearMonth.now().toString() : mes;
        Optional<ElegibilidadeDto> elegibilidadeOpt = elegibilidadeService.avaliar(cpf, mesEfetivo);

        if (elegibilidadeOpt.isEmpty()) {
            return "retiradas/registrar";
        }

        ElegibilidadeDto elegibilidade = elegibilidadeOpt.get();
        model.addAttribute("elegibilidade", elegibilidade);

        RetiradaForm form = new RetiradaForm();
        form.setCpfBeneficiario(elegibilidade.getCpf());
        form.setMesReferencia(mesEfetivo);
        form.setDataRetirada(LocalDateTime.now().format(DATA_HORA_FORM));
        form.setQuantidadeCestas("1");
        model.addAttribute("retiradaForm", form);
        prepararModelDoFormularioRetirada(model);

        return "retiradas/registrar";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("retiradaForm") RetiradaForm form,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        retiradaFormValidator.validate(form, bindingResult);
        if (bindingResult.hasErrors()) {
            return reexibirFormulario(form, model);
        }

        Beneficiario beneficiario = beneficiarioService.buscarPorCpf(form.getCpfBeneficiario()).orElse(null);
        if (beneficiario == null) {
            model.addAttribute("erro", "Beneficiário não encontrado para o CPF informado.");
            return reexibirFormulario(form, model);
        }

        List<Aviso> avisos = validacaoRetiradaService.avaliar(new ContextoAvaliacaoRetirada(form, beneficiario));
        if (!avisos.isEmpty()) {
            prepararModelDaConfirmacao(model, form, avisos);
            return "retiradas/confirmacao";
        }

        try {
            RetiradaCesta salva = retiradaService.salvar(form, List.of(), null, usuarioLogado());
            redirectAttributes.addFlashAttribute("sucesso", "Retirada registrada com sucesso.");
            return "redirect:/beneficios/retiradas/" + salva.getId() + "/comprovante";
        } catch (RetiradaConcorrenteException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            redirectAttributes.addAttribute("cpf", form.getCpfBeneficiario());
            redirectAttributes.addAttribute("mes", form.getMesReferencia());
            return "redirect:/beneficios/retiradas/registrar";
        }
    }

    @PostMapping("/confirmar")
    public String confirmar(@ModelAttribute("retiradaForm") RetiradaForm form,
                             BindingResult bindingResult,
                             @RequestParam(name = "avisosConfirmados", required = false) List<String> codigosConfirmados,
                             @RequestParam(required = false) String justificativa,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        retiradaFormValidator.validate(form, bindingResult);
        if (bindingResult.hasErrors()) {
            return reexibirFormulario(form, model);
        }

        Beneficiario beneficiario = beneficiarioService.buscarPorCpf(form.getCpfBeneficiario()).orElse(null);
        if (beneficiario == null) {
            model.addAttribute("erro", "Beneficiário não encontrado para o CPF informado.");
            return reexibirFormulario(form, model);
        }

        // Reavalia: os dados podem ter mudado entre a tela de formulário e esta
        // (outro atendente registrou uma retirada nesse meio-tempo, por exemplo).
        List<Aviso> avisosAtuais = validacaoRetiradaService.avaliar(new ContextoAvaliacaoRetirada(form, beneficiario));
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
            return "retiradas/confirmacao";
        }

        List<Aviso> avisosParaPersistir = avisosAtuais.stream()
                .filter(aviso -> !aviso.isExigeJustificativa() || confirmados.contains(aviso.getCodigo()))
                .toList();

        try {
            RetiradaCesta salva = retiradaService.salvar(form, avisosParaPersistir, justificativa, usuarioLogado());
            redirectAttributes.addFlashAttribute("sucesso", "Retirada registrada com sucesso — avisos confirmados registrados.");
            return "redirect:/beneficios/retiradas/" + salva.getId() + "/comprovante";
        } catch (RetiradaConcorrenteException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            redirectAttributes.addAttribute("cpf", form.getCpfBeneficiario());
            redirectAttributes.addAttribute("mes", form.getMesReferencia());
            return "redirect:/beneficios/retiradas/registrar";
        }
    }

    /** "Voltar e corrigir": reexibe o formulário com tudo que foi digitado, sem revalidar nada aqui. */
    @PostMapping("/voltar")
    public String voltar(@ModelAttribute("retiradaForm") RetiradaForm form, Model model) {
        return reexibirFormulario(form, model);
    }

    @GetMapping("/{id}/comprovante")
    public String comprovante(@PathVariable Long id, Model model) {
        model.addAttribute("tituloPagina", "Comprovante de retirada");
        model.addAttribute("comprovante", retiradaService.comprovante(id));
        return "retiradas/comprovante";
    }

    @GetMapping
    public String historico(RetiradaFiltro filtro,
                             @PageableDefault(size = 20, sort = "dataRetirada", direction = Sort.Direction.DESC) Pageable pageable,
                             Model model) {
        Page<RetiradaListaItemDto> pagina = retiradaService.listar(filtro, pageable);

        model.addAttribute("tituloPagina", "Histórico de retiradas");
        model.addAttribute("pagina", pagina);
        model.addAttribute("filtro", filtro);
        model.addAttribute("tiposRetirante", tiposRetiranteOpcoes());
        model.addAttribute("padrinhos", voluntarioRepository.findByPadrinhoTrueAndAtivoTrue());
        model.addAttribute("voluntarios", voluntarioRepository.findByAtivoTrue());

        return "retiradas/historico";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("tituloPagina", "Detalhe da retirada");
        model.addAttribute("detalhe", retiradaService.detalhar(id));
        return "retiradas/detalhe";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, @RequestParam String motivo, RedirectAttributes redirectAttributes) {
        retiradaService.cancelar(id, motivo, usuarioLogado());
        redirectAttributes.addFlashAttribute("sucesso", "Retirada cancelada com sucesso.");
        return "redirect:/beneficios/retiradas/" + id;
    }

    // ---- helpers privados de Model ----

    private String reexibirFormulario(RetiradaForm form, Model model) {
        model.addAttribute("tituloPagina", "Registrar retirada");
        model.addAttribute("cpfPesquisado", form.getCpfBeneficiario());
        model.addAttribute("retiradaForm", form);

        elegibilidadeService.avaliar(form.getCpfBeneficiario(), form.getMesReferencia())
                .ifPresent(dto -> model.addAttribute("elegibilidade", dto));

        prepararModelDoFormularioRetirada(model);
        return "retiradas/registrar";
    }

    private void prepararModelDoFormularioRetirada(Model model) {
        model.addAttribute("tiposRetirante", tiposRetiranteOpcoes());
        model.addAttribute("padrinhos", voluntarioRepository.findByPadrinhoTrueAndAtivoTrue());
        model.addAttribute("voluntarios", voluntarioRepository.findByAtivoTrue());
        model.addAttribute("mesesDisponiveis", mesesDisponiveis());
    }

    private void prepararModelDaConfirmacao(Model model, RetiradaForm form, List<Aviso> avisos) {
        model.addAttribute("tituloPagina", "Confirmar retirada");
        model.addAttribute("retiradaForm", form);
        model.addAttribute("avisos", avisos);
        model.addAttribute("exigeJustificativa", avisos.stream().anyMatch(Aviso::isExigeJustificativa));
        model.addAttribute("camposOcultos", FormUtils.paraCamposOcultos(form));
    }

    private List<String> mesesDisponiveis() {
        YearMonth atual = YearMonth.now();
        return List.of(atual.toString(), atual.minusMonths(1).toString(), atual.minusMonths(2).toString(), atual.minusMonths(3).toString());
    }

    private Map<String, String> tiposRetiranteOpcoes() {
        Map<String, String> opcoes = new LinkedHashMap<>();
        for (TipoRetirante tipo : TipoRetirante.values()) {
            opcoes.put(tipo.name(), tipo.getDescricao());
        }
        return opcoes;
    }

    /** {@code registrado_por}/{@code cancelada_por} vêm sempre do usuário autenticado, nunca do formulário. */
    private String usuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario.getNome();
        }
        return "sistema";
    }
}
