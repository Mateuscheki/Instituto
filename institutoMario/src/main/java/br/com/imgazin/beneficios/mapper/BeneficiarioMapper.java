package br.com.imgazin.beneficios.mapper;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.BeneficiarioEndereco;
import br.com.imgazin.beneficios.domain.RespostaSimNao;
import br.com.imgazin.beneficios.domain.RetiradaCesta;
import br.com.imgazin.beneficios.domain.Voluntario;
import br.com.imgazin.beneficios.dto.BeneficiarioFichaDto;
import br.com.imgazin.beneficios.dto.BeneficiarioListaItemDto;
import br.com.imgazin.beneficios.dto.RetiradaResumoDto;
import br.com.imgazin.beneficios.form.BeneficiarioForm;
import br.com.imgazin.beneficios.util.CpfUtils;
import br.com.imgazin.beneficios.util.EnderecoUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Conversão Form <-> Entidade <-> DTO. Fica tudo aqui de propósito — nem
 * controller nem service fazem parsing de string de formulário na mão (ver
 * convenções do CLAUDE.md: "Entidade JPA nunca vai para a view").
 */
@Component
public class BeneficiarioMapper {

    public BeneficiarioForm paraForm(Beneficiario beneficiario) {
        BeneficiarioForm form = new BeneficiarioForm();
        form.setId(beneficiario.getId());
        form.setCpf(CpfUtils.formatar(beneficiario.getCpf()));
        form.setNome(beneficiario.getNome());
        form.setDataNascimento(toIso(beneficiario.getDataNascimento()));
        form.setTelefone(beneficiario.getTelefone());
        form.setEmail(beneficiario.getEmail());
        form.setNomeMae(beneficiario.getNomeMae());
        form.setNomeConjuge(beneficiario.getNomeConjuge());
        form.setCpfConjuge(beneficiario.getCpfConjuge() == null ? null : CpfUtils.formatar(beneficiario.getCpfConjuge()));
        form.setPessoasResidencia(toString(beneficiario.getPessoasResidencia()));
        form.setTemFilhos(toName(beneficiario.getTemFilhos()));
        form.setQuantidadeFilhos(toString(beneficiario.getQuantidadeFilhos()));
        form.setIdadesFilhos(beneficiario.getIdadesFilhos());
        form.setFilhosComDeficiencia(toName(beneficiario.getFilhosComDeficiencia()));
        form.setDescricaoDeficienciaFilhos(beneficiario.getDescricaoDeficienciaFilhos());
        form.setFamiliarComDeficiencia(toName(beneficiario.getFamiliarComDeficiencia()));
        form.setDescricaoDeficienciaFamiliar(beneficiario.getDescricaoDeficienciaFamiliar());
        form.setTemTrabalho(toName(beneficiario.getTemTrabalho()));
        form.setRendaFamiliar(beneficiario.getRendaFamiliar() == null ? null : beneficiario.getRendaFamiliar().toPlainString());
        form.setStatus(toName(beneficiario.getStatus()));
        form.setPrazoFinalBeneficio(toIso(beneficiario.getPrazoFinalBeneficio()));
        form.setPadrinhoId(beneficiario.getPadrinho() == null ? null : beneficiario.getPadrinho().getId().toString());
        form.setObservacao(beneficiario.getObservacao());

        BeneficiarioEndereco endereco = beneficiario.getEndereco();
        if (endereco != null) {
            form.setEnderecoCompleto(endereco.getEnderecoCompleto());
            form.setCep(endereco.getCep());
            form.setRua(endereco.getRua());
            form.setNumero(endereco.getNumero());
            form.setComplemento(endereco.getComplemento());
            form.setBairro(endereco.getBairro());
            form.setCidade(endereco.getCidade());
            form.setUf(endereco.getUf());
        }
        return form;
    }

    /** Aplica os dados do form na entidade (nova ou existente). Não mexe em status/ativo — decisão do service. */
    public void aplicarNaEntidade(BeneficiarioForm form, Beneficiario entidade, Voluntario padrinho) {
        entidade.setCpf(CpfUtils.normalizar(form.getCpf()));
        entidade.setNome(form.getNome().trim());
        entidade.setDataNascimento(parseData(form.getDataNascimento()));
        entidade.setTelefone(blankToNull(form.getTelefone()));
        entidade.setEmail(blankToNull(form.getEmail()));
        entidade.setNomeMae(blankToNull(form.getNomeMae()));
        entidade.setNomeConjuge(blankToNull(form.getNomeConjuge()));
        entidade.setCpfConjuge(isBlank(form.getCpfConjuge()) ? null : CpfUtils.normalizar(form.getCpfConjuge()));
        entidade.setPessoasResidencia(parseInteiro(form.getPessoasResidencia()));

        RespostaSimNao temFilhos = parseRespostaSimNao(form.getTemFilhos());
        entidade.setTemFilhos(temFilhos);
        if (temFilhos == RespostaSimNao.SIM) {
            entidade.setQuantidadeFilhos(parseInteiro(form.getQuantidadeFilhos()));
            entidade.setIdadesFilhos(blankToNull(form.getIdadesFilhos()));
        } else {
            // Espelha no servidor a regra de UI "tem_filhos=NAO desabilita e limpa" —
            // não confia só no JS para não gravar dado inconsistente.
            entidade.setQuantidadeFilhos(null);
            entidade.setIdadesFilhos(null);
        }

        entidade.setFilhosComDeficiencia(parseRespostaSimNao(form.getFilhosComDeficiencia()));
        entidade.setDescricaoDeficienciaFilhos(blankToNull(form.getDescricaoDeficienciaFilhos()));
        entidade.setFamiliarComDeficiencia(parseRespostaSimNao(form.getFamiliarComDeficiencia()));
        entidade.setDescricaoDeficienciaFamiliar(blankToNull(form.getDescricaoDeficienciaFamiliar()));
        entidade.setTemTrabalho(parseRespostaSimNao(form.getTemTrabalho()));
        entidade.setRendaFamiliar(parseRenda(form.getRendaFamiliar()));
        entidade.setPrazoFinalBeneficio(parseData(form.getPrazoFinalBeneficio()));
        entidade.setPadrinho(padrinho);
        entidade.setObservacao(blankToNull(form.getObservacao()));
    }

    /** Cria/atualiza o endereço 1:1 e recalcula a chave de residência (regra de negócio #4 do módulo). */
    public void aplicarNoEndereco(BeneficiarioForm form, BeneficiarioEndereco endereco, Beneficiario beneficiario) {
        endereco.setBeneficiario(beneficiario);
        endereco.setEnderecoCompleto(blankToNull(form.getEnderecoCompleto()));
        endereco.setCep(EnderecoUtils.normalizarCep(form.getCep()));
        endereco.setRua(blankToNull(form.getRua()));
        endereco.setNumero(blankToNull(form.getNumero()));
        endereco.setComplemento(blankToNull(form.getComplemento()));
        endereco.setBairro(blankToNull(form.getBairro()));
        endereco.setCidade(blankToNull(form.getCidade()));
        endereco.setUf(isBlank(form.getUf()) ? null : form.getUf().trim().toUpperCase());
        endereco.setChaveResidencia(EnderecoUtils.calcularChaveResidencia(form.getCep(), form.getNumero(), form.getComplemento()));
    }

    public BeneficiarioListaItemDto paraItemLista(Beneficiario beneficiario) {
        BeneficiarioEndereco endereco = beneficiario.getEndereco();
        return new BeneficiarioListaItemDto(
                beneficiario.getId(),
                beneficiario.getCpf(),
                beneficiario.getNome(),
                endereco == null ? null : endereco.getCidade(),
                endereco == null ? null : endereco.getBairro(),
                beneficiario.getStatus(),
                beneficiario.getPrazoFinalBeneficio(),
                beneficiario.getPadrinho() == null ? null : beneficiario.getPadrinho().getNome(),
                beneficiario.isAtivo()
        );
    }

    public BeneficiarioFichaDto paraFicha(Beneficiario beneficiario, List<RetiradaCesta> ultimasRetiradas, boolean retirouMesAtual) {
        BeneficiarioEndereco endereco = beneficiario.getEndereco();

        BeneficiarioFichaDto dto = new BeneficiarioFichaDto();
        dto.setId(beneficiario.getId());
        dto.setCpf(beneficiario.getCpf());
        dto.setNome(beneficiario.getNome());
        dto.setDataNascimento(beneficiario.getDataNascimento());
        dto.setTelefone(beneficiario.getTelefone());
        dto.setEmail(beneficiario.getEmail());
        dto.setNomeMae(beneficiario.getNomeMae());
        dto.setNomeConjuge(beneficiario.getNomeConjuge());
        dto.setCpfConjuge(beneficiario.getCpfConjuge());
        dto.setPessoasResidencia(beneficiario.getPessoasResidencia());
        dto.setTemFilhos(beneficiario.getTemFilhos());
        dto.setQuantidadeFilhos(beneficiario.getQuantidadeFilhos());
        dto.setIdadesFilhos(beneficiario.getIdadesFilhos());
        dto.setFilhosComDeficiencia(beneficiario.getFilhosComDeficiencia());
        dto.setDescricaoDeficienciaFilhos(beneficiario.getDescricaoDeficienciaFilhos());
        dto.setFamiliarComDeficiencia(beneficiario.getFamiliarComDeficiencia());
        dto.setDescricaoDeficienciaFamiliar(beneficiario.getDescricaoDeficienciaFamiliar());
        dto.setTemTrabalho(beneficiario.getTemTrabalho());
        dto.setRendaFamiliar(beneficiario.getRendaFamiliar());
        dto.setStatus(beneficiario.getStatus());
        dto.setPrazoFinalBeneficio(beneficiario.getPrazoFinalBeneficio());
        dto.setObservacao(beneficiario.getObservacao());
        dto.setAtivo(beneficiario.isAtivo());
        dto.setPadrinhoId(beneficiario.getPadrinho() == null ? null : beneficiario.getPadrinho().getId());
        dto.setPadrinhoNome(beneficiario.getPadrinho() == null ? null : beneficiario.getPadrinho().getNome());

        if (endereco != null) {
            dto.setEnderecoCompleto(endereco.getEnderecoCompleto());
            dto.setCep(endereco.getCep());
            dto.setRua(endereco.getRua());
            dto.setNumero(endereco.getNumero());
            dto.setComplemento(endereco.getComplemento());
            dto.setBairro(endereco.getBairro());
            dto.setCidade(endereco.getCidade());
            dto.setUf(endereco.getUf());
        }

        dto.setUltimasRetiradas(ultimasRetiradas.stream().map(this::paraRetiradaResumo).toList());
        dto.setRetirouMesAtual(retirouMesAtual);

        dto.setCriadoEm(beneficiario.getCriadoEm());
        dto.setCriadoPor(beneficiario.getCriadoPor());
        dto.setAtualizadoEm(beneficiario.getAtualizadoEm());
        dto.setAtualizadoPor(beneficiario.getAtualizadoPor());

        return dto;
    }

    private RetiradaResumoDto paraRetiradaResumo(RetiradaCesta retirada) {
        return new RetiradaResumoDto(
                retirada.getId(),
                retirada.getMesReferencia(),
                retirada.getDataRetirada(),
                retirada.getTipoRetirante(),
                retirada.getRetiranteNome(),
                retirada.isCancelada()
        );
    }

    // ---- helpers de parsing (tolerantes: já foram validados por BeneficiarioFormValidator antes de chegar aqui) ----

    private boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }

    private String blankToNull(String valor) {
        return isBlank(valor) ? null : valor.trim();
    }

    private String toString(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private String toName(Enum<?> valor) {
        return valor == null ? null : valor.name();
    }

    private String toIso(LocalDate data) {
        return data == null ? null : data.toString();
    }

    private LocalDate parseData(String valor) {
        if (isBlank(valor)) {
            return null;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInteiro(String valor) {
        if (isBlank(valor)) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseRenda(String valor) {
        if (isBlank(valor)) {
            return null;
        }
        try {
            return new BigDecimal(valor.trim().replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private RespostaSimNao parseRespostaSimNao(String valor) {
        if (isBlank(valor)) {
            return null;
        }
        try {
            return RespostaSimNao.valueOf(valor.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
