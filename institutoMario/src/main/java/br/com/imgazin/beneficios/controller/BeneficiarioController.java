package br.com.imgazin.beneficios.controller;

import br.com.imgazin.beneficios.aviso.Aviso;
import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.RespostaSimNao;
import br.com.imgazin.beneficios.domain.SeveridadeAviso;
import br.com.imgazin.beneficios.domain.StatusBeneficiario;
import br.com.imgazin.beneficios.dto.BeneficiarioListaItemDto;
import br.com.imgazin.beneficios.exception.CpfDuplicadoException;
import br.com.imgazin.beneficios.form.BeneficiarioFiltro;
import br.com.imgazin.beneficios.form.BeneficiarioForm;
import br.com.imgazin.beneficios.mapper.BeneficiarioMapper;
import br.com.imgazin.beneficios.repository.VoluntarioRepository;
import br.com.imgazin.beneficios.service.BeneficiarioService;
import br.com.imgazin.beneficios.service.RetiradaService;
import br.com.imgazin.beneficios.service.ValidacaoBeneficiarioService;
import br.com.imgazin.beneficios.util.FormUtils;
import br.com.imgazin.beneficios.validation.BeneficiarioFormValidator;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller fino (ver CLAUDE.md): recebe form, chama service, popula
 * Model, devolve a view. Nenhuma regra de negócio aqui — inclusive a
 * verificação de CPF duplicado e o motor de avisos vivem nos services.
 */
@Controller
@RequestMapping("/beneficios/beneficiarios")
@RequiredArgsConstructor
public class BeneficiarioController {

    private final BeneficiarioService beneficiarioService;
    private final ValidacaoBeneficiarioService validacaoBeneficiarioService;
    private final BeneficiarioFormValidator beneficiarioFormValidator;
    private final BeneficiarioMapper mapper;
    private final VoluntarioRepository voluntarioRepository;
    private final RetiradaService retiradaService;

    @InitBinder("beneficiarioForm")
    public void initBinder(WebDataBinder binder) {
        // O único campo não-String do Form é o id (hidden, repostado na tela de
        // confirmação) — sem isso, um id nulo vira "" no hidden e quebra o bind.
        binder.registerCustomEditor(Long.class, new CustomNumberEditor(Long.class, true));
    }

    @GetMapping("/busca")
    public String busca(@RequestParam(required = false) String cpf, Model model) {
        model.addAttribute("tituloPagina", "Buscar beneficiário");
        model.addAttribute("cpfPesquisado", cpf);

        if (cpf != null && !cpf.isBlank()) {
            Optional<Beneficiario> encontrado = beneficiarioService.buscarPorCpf(cpf);
            if (encontrado.isPresent()) {
                model.addAttribute("resultado", mapper.paraItemLista(encontrado.get()));
            } else {
                model.addAttribute("naoEncontrado", true);
            }
        }

        return "beneficios/beneficiarios/busca";
    }

    @GetMapping
    public String listar(BeneficiarioFiltro filtro,
                          @PageableDefault(size = 20, sort = "nome") Pageable pageable,
                          Model model) {
        Page<BeneficiarioListaItemDto> pagina = beneficiarioService.listar(filtro, pageable);

        model.addAttribute("tituloPagina", "Beneficiários");
        model.addAttribute("pagina", pagina);
        model.addAttribute("filtro", filtro);
        model.addAttribute("statusOpcoes", statusOpcoes());
        model.addAttribute("padrinhos", voluntarioRepository.findByPadrinhoTrueAndAtivoTrue());

        return "beneficios/beneficiarios/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) String cpf, Model model) {
        BeneficiarioForm form = new BeneficiarioForm();
        form.setCpf(cpf); // vindo da busca sem resultado — poupa o operador de redigitar

        model.addAttribute("tituloPagina", "Novo beneficiário");
        model.addAttribute("beneficiarioForm", form);
        prepararModelDoFormulario(model);
        return "beneficios/beneficiarios/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Beneficiario entidade = beneficiarioService.buscarPorId(id);
        model.addAttribute("tituloPagina", "Editar beneficiário");
        model.addAttribute("beneficiarioForm", mapper.paraForm(entidade));
        prepararModelDoFormulario(model);
        return "beneficios/beneficiarios/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("beneficiarioForm") BeneficiarioForm form,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        beneficiarioFormValidator.validate(form, bindingResult);
        verificarCpfDuplicadoSeNecessario(form, bindingResult, model);

        if (bindingResult.hasErrors()) {
            model.addAttribute("tituloPagina", form.getId() == null ? "Novo beneficiário" : "Editar beneficiário");
            prepararModelDoFormulario(model);
            return "beneficios/beneficiarios/formulario";
        }

        List<Aviso> avisos = validacaoBeneficiarioService.avaliar(form);
        if (!avisos.isEmpty()) {
            prepararModelDaConfirmacao(model, form, avisos);
            return "beneficios/beneficiarios/confirmacao";
        }

        Beneficiario salvo = beneficiarioService.salvar(form, List.of(), null);
        redirectAttributes.addFlashAttribute("sucesso", "Beneficiário salvo com sucesso.");
        return "redirect:/beneficios/beneficiarios/" + salvo.getId();
    }

    @PostMapping("/confirmar")
    public String confirmar(@ModelAttribute("beneficiarioForm") BeneficiarioForm form,
                             BindingResult bindingResult,
                             @RequestParam(name = "avisosConfirmados", required = false) List<String> codigosConfirmados,
                             @RequestParam(required = false) String justificativa,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        beneficiarioFormValidator.validate(form, bindingResult);
        verificarCpfDuplicadoSeNecessario(form, bindingResult, model);

        if (bindingResult.hasErrors()) {
            model.addAttribute("tituloPagina", form.getId() == null ? "Novo beneficiário" : "Editar beneficiário");
            prepararModelDoFormulario(model);
            return "beneficios/beneficiarios/formulario";
        }

        // Reavalia: os dados podem ter mudado entre a tela de formulário e esta
        // (outro cadastro criado nesse meio-tempo, prazo virando "vencido", etc.).
        List<Aviso> avisosAtuais = validacaoBeneficiarioService.avaliar(form);
        List<String> confirmados = codigosConfirmados == null ? List.of() : codigosConfirmados;

        List<Aviso> altasNaoConfirmados = avisosAtuais.stream()
                .filter(Aviso::isExigeJustificativa)
                .filter(aviso -> !confirmados.contains(aviso.getCodigo()))
                .toList();

        boolean exigeJustificativa = avisosAtuais.stream().anyMatch(Aviso::isExigeJustificativa);
        boolean faltaJustificativa = exigeJustificativa && (justificativa == null || justificativa.isBlank());

        if (!altasNaoConfirmados.isEmpty() || faltaJustificativa) {
            // Exigência de confirmação explícita, não recusa: volta para a MESMA
            // tela de confirmação com o que faltou destacado.
            prepararModelDaConfirmacao(model, form, avisosAtuais);
            model.addAttribute("avisosDestacados", altasNaoConfirmados);
            model.addAttribute("avisosJaConfirmados", confirmados);
            model.addAttribute("justificativaDigitada", justificativa);
            model.addAttribute("erroConfirmacao", faltaJustificativa
                    ? "Informe a justificativa antes de confirmar."
                    : "Confirme todos os avisos de alta severidade antes de prosseguir.");
            return "beneficios/beneficiarios/confirmacao";
        }

        List<Aviso> avisosParaPersistir = avisosAtuais.stream()
                .filter(aviso -> !aviso.isExigeJustificativa() || confirmados.contains(aviso.getCodigo()))
                .toList();

        Beneficiario salvo = beneficiarioService.salvar(form, avisosParaPersistir, justificativa);
        redirectAttributes.addFlashAttribute("sucesso", "Beneficiário salvo com sucesso — avisos confirmados registrados.");
        return "redirect:/beneficios/beneficiarios/" + salvo.getId();
    }

    /**
     * "Voltar e corrigir" na tela de confirmação: reposta os mesmos hidden
     * (ver {@code fragments/campos.html :: hiddensDoForm}) e volta para o
     * formulário SEM perder nada digitado — sem revalidar nada aqui, quem
     * decide se está tudo certo é o próximo /salvar.
     */
    @PostMapping("/voltar")
    public String voltarParaFormulario(@ModelAttribute("beneficiarioForm") BeneficiarioForm form, Model model) {
        model.addAttribute("tituloPagina", form.getId() == null ? "Novo beneficiário" : "Editar beneficiário");
        prepararModelDoFormulario(model);
        return "beneficios/beneficiarios/formulario";
    }

    @GetMapping("/{id}")
    public String ficha(@PathVariable Long id, Model model) {
        model.addAttribute("tituloPagina", "Ficha do beneficiário");
        model.addAttribute("ficha", beneficiarioService.montarFicha(id));
        model.addAttribute("statusOpcoes", statusOpcoes());
        model.addAttribute("voluntariosAtivos", voluntarioRepository.findByAtivoTrue());
        return "beneficios/beneficiarios/ficha";
    }

    @GetMapping("/{id}/retiradas")
    public String historicoDeRetiradas(@PathVariable Long id, Model model) {
        model.addAttribute("tituloPagina", "Histórico de retiradas");
        model.addAttribute("historico", retiradaService.historicoDoBeneficiario(id));
        return "beneficios/beneficiarios/retiradas";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id,
                                 @RequestParam String novoStatus,
                                 @RequestParam String motivo,
                                 RedirectAttributes redirectAttributes) {
        beneficiarioService.alterarStatus(id, novoStatus, motivo);
        redirectAttributes.addFlashAttribute("sucesso", "Status atualizado com sucesso.");
        return "redirect:/beneficios/beneficiarios/" + id;
    }

    @PostMapping("/{id}/inativar")
    public String inativar(@PathVariable Long id,
                            @RequestParam String motivo,
                            RedirectAttributes redirectAttributes) {
        beneficiarioService.inativar(id, motivo);
        redirectAttributes.addFlashAttribute("sucesso", "Beneficiário inativado com sucesso.");
        return "redirect:/beneficios/beneficiarios/" + id;
    }

    // ---- helpers privados de Model (controller continua fino: só monta o que a view precisa) ----

    private void verificarCpfDuplicadoSeNecessario(BeneficiarioForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasFieldErrors("cpf")) {
            return; // formato já inválido — não faz sentido checar duplicidade
        }
        try {
            beneficiarioService.verificarCpfDuplicado(form);
        } catch (CpfDuplicadoException e) {
            bindingResult.rejectValue("cpf", "cpf.duplicado", "Já existe um beneficiário cadastrado com este CPF.");
            model.addAttribute("cpfDuplicadoId", e.getIdExistente());
        }
    }

    private void prepararModelDoFormulario(Model model) {
        model.addAttribute("statusOpcoes", statusOpcoes());
        model.addAttribute("respostaSimNaoOpcoes", respostaSimNaoOpcoes());
        model.addAttribute("padrinhos", voluntarioRepository.findByPadrinhoTrueAndAtivoTrue());
    }

    private void prepararModelDaConfirmacao(Model model, BeneficiarioForm form, List<Aviso> avisos) {
        model.addAttribute("tituloPagina", "Confirmar cadastro");
        model.addAttribute("beneficiarioForm", form);
        model.addAttribute("avisos", avisos);
        model.addAttribute("avisosPorSeveridade", agruparPorSeveridade(avisos));
        model.addAttribute("exigeJustificativa", avisos.stream().anyMatch(Aviso::isExigeJustificativa));
        model.addAttribute("camposOcultos", FormUtils.paraCamposOcultos(form));
    }

    private Map<SeveridadeAviso, List<Aviso>> agruparPorSeveridade(List<Aviso> avisos) {
        return avisos.stream().collect(Collectors.groupingBy(Aviso::getSeveridade));
    }

    private Map<String, String> statusOpcoes() {
        Map<String, String> opcoes = new LinkedHashMap<>();
        for (StatusBeneficiario status : StatusBeneficiario.values()) {
            opcoes.put(status.name(), status.getDescricao());
        }
        return opcoes;
    }

    private Map<String, String> respostaSimNaoOpcoes() {
        Map<String, String> opcoes = new LinkedHashMap<>();
        for (RespostaSimNao resposta : RespostaSimNao.values()) {
            opcoes.put(resposta.name(), resposta.getDescricao());
        }
        return opcoes;
    }
}
