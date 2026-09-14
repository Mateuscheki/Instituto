package br.com.imgazin.beneficios.repository;

import br.com.imgazin.beneficios.domain.SeveridadeAviso;
import br.com.imgazin.beneficios.domain.TipoRetirante;
import br.com.imgazin.beneficios.dto.ConfirmacaoRelatorioDto;
import br.com.imgazin.beneficios.dto.PorPadrinhoDto;
import br.com.imgazin.beneficios.dto.PorRegiaoDto;
import br.com.imgazin.beneficios.dto.PorVoluntarioDto;
import br.com.imgazin.beneficios.dto.RetiradaPorTerceiroDto;
import br.com.imgazin.beneficios.dto.VoluntarioAnaliticoDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consultas de leitura para os relatórios — sempre agregando via JPQL/SQL
 * direto no banco, nunca carregando entidade para somar em memória (ver
 * CLAUDE.md, Etapa 5). Fica numa classe própria (não uma interface Spring
 * Data comum) porque cada relatório combina parâmetros de um jeito
 * diferente demais para caber bem em métodos derivados/@Query fixos.
 */
@Repository
@RequiredArgsConstructor
public class RelatorioRepository {

    private final EntityManager entityManager;

    // ---- Dashboard / contadores simples ----

    public long contarBeneficiariosAtivos() {
        return numero(entityManager.createQuery("select count(b) from Beneficiario b where b.ativo = true").getSingleResult());
    }

    public long contarPorStatusAtivo(br.com.imgazin.beneficios.domain.StatusBeneficiario status) {
        return numero(entityManager.createQuery(
                        "select count(b) from Beneficiario b where b.ativo = true and b.status = :status")
                .setParameter("status", status).getSingleResult());
    }

    public long contarBeneficiariosDistintosComRetiradaNoMes(LocalDate mes) {
        return numero(entityManager.createQuery(
                        "select count(distinct r.beneficiario.id) from RetiradaCesta r where r.mesReferencia = :mes and r.cancelada = false")
                .setParameter("mes", mes).getSingleResult());
    }

    public long somarCestasNoMes(LocalDate mes) {
        Object resultado = entityManager.createQuery(
                        "select coalesce(sum(r.quantidadeCestas), 0) from RetiradaCesta r where r.mesReferencia = :mes and r.cancelada = false")
                .setParameter("mes", mes).getSingleResult();
        return numero(resultado);
    }

    public long contarNovosCadastros(LocalDateTime inicio, LocalDateTime fim) {
        return numero(entityManager.createQuery(
                        "select count(b) from Beneficiario b where b.criadoEm between :inicio and :fim")
                .setParameter("inicio", inicio).setParameter("fim", fim).getSingleResult());
    }

    public long contarPrazoVencendoEm(int dias) {
        LocalDate hoje = LocalDate.now();
        return numero(entityManager.createQuery(
                        "select count(b) from Beneficiario b where b.ativo = true and b.prazoFinalBeneficio between :hoje and :limite")
                .setParameter("hoje", hoje).setParameter("limite", hoje.plusDays(dias)).getSingleResult());
    }

    public long contarVoluntariosAtivos() {
        return numero(entityManager.createQuery("select count(v) from Voluntario v where v.ativo = true").getSingleResult());
    }

    public long contarPadrinhosAtivos() {
        return numero(entityManager.createQuery("select count(v) from Voluntario v where v.ativo = true and v.padrinho = true").getSingleResult());
    }

    // ---- Séries e agrupamentos ----

    /** [mes, retiradas, cestas, beneficiariosDistintos] por mês, no intervalo (ambos inclusive). */
    @SuppressWarnings("unchecked")
    public List<Object[]> serieRetiradasPorMes(LocalDate inicio, LocalDate fim) {
        return entityManager.createQuery(
                        "select r.mesReferencia, count(r), coalesce(sum(r.quantidadeCestas),0), count(distinct r.beneficiario.id) " +
                                "from RetiradaCesta r where r.mesReferencia between :inicio and :fim and r.cancelada = false " +
                                "group by r.mesReferencia order by r.mesReferencia")
                .setParameter("inicio", inicio).setParameter("fim", fim)
                .getResultList();
    }

    /** [tipoRetirante, quantidade] das retiradas válidas no mês. */
    @SuppressWarnings("unchecked")
    public List<Object[]> distribuicaoTipoRetiranteNoMes(LocalDate mes) {
        return entityManager.createQuery(
                        "select r.tipoRetirante, count(r) from RetiradaCesta r " +
                                "where r.mesReferencia = :mes and r.cancelada = false group by r.tipoRetirante")
                .setParameter("mes", mes).getResultList();
    }

    /** [tipoRetirante, quantidade] no período, excluindo PROPRIO — indicador de controle interno. */
    @SuppressWarnings("unchecked")
    public List<Object[]> distribuicaoTipoRetiranteTerceirosNoPeriodo(LocalDate inicio, LocalDate fim) {
        return entityManager.createQuery(
                        "select r.tipoRetirante, count(r) from RetiradaCesta r " +
                                "where r.mesReferencia between :inicio and :fim and r.cancelada = false " +
                                "and r.tipoRetirante <> br.com.imgazin.beneficios.domain.TipoRetirante.PROPRIO " +
                                "group by r.tipoRetirante")
                .setParameter("inicio", inicio).setParameter("fim", fim).getResultList();
    }

    /** Distribuição geográfica: agrupa por bairro, cidade ou uf (nome da coluna já validado pelo controller). */
    @SuppressWarnings("unchecked")
    public List<PorRegiaoDto> porRegiao(String colunaAgrupamento, LocalDate inicio, LocalDate fim) {
        String coluna = switch (colunaAgrupamento) {
            case "cidade" -> "e.cidade";
            case "uf" -> "e.uf";
            default -> "e.bairro";
        };

        String sql = "select coalesce(" + coluna + ", '(não informado)') as regiao, " +
                "count(distinct b.id) as beneficiarios, " +
                "count(distinct case when r.id is not null and r.cancelada = false and r.mes_referencia between :inicio and :fim then r.id end) as retiradas " +
                "from beneficiario b " +
                "left join beneficiario_endereco e on e.beneficiario_id = b.id " +
                "left join retirada_cesta r on r.beneficiario_id = b.id " +
                "where b.ativo = true " +
                "group by " + coluna + " order by beneficiarios desc";

        Query query = entityManager.createNativeQuery(sql)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim);

        List<Object[]> linhas = query.getResultList();
        return linhas.stream()
                .map(linha -> new PorRegiaoDto((String) linha[0], numero(linha[1]), numero(linha[2])))
                .toList();
    }

    // ---- Por padrinho / por voluntário ----

    public long contarApadrinhados(Long padrinhoId) {
        return numero(entityManager.createQuery("select count(b) from Beneficiario b where b.padrinho.id = :id")
                .setParameter("id", padrinhoId).getSingleResult());
    }

    public long contarRetiradasDeApadrinhadosNoPeriodo(Long padrinhoId, LocalDate inicio, LocalDate fim) {
        return numero(entityManager.createQuery(
                        "select count(distinct r.beneficiario.id) from RetiradaCesta r " +
                                "where r.beneficiario.padrinho.id = :id and r.cancelada = false and r.mesReferencia between :inicio and :fim")
                .setParameter("id", padrinhoId).setParameter("inicio", inicio).setParameter("fim", fim).getSingleResult());
    }

    public long contarRetiradasFeitasPeloPadrinho(Long padrinhoId, LocalDate inicio, LocalDate fim) {
        return numero(entityManager.createQuery(
                        "select count(r) from RetiradaCesta r where r.padrinho.id = :id and r.retiradoPorPadrinho = true " +
                                "and r.cancelada = false and r.mesReferencia between :inicio and :fim")
                .setParameter("id", padrinhoId).setParameter("inicio", inicio).setParameter("fim", fim).getSingleResult());
    }

    public long contarEntregasNoPeriodo(Long voluntarioId, LocalDate inicio, LocalDate fim) {
        return numero(entityManager.createQuery(
                        "select count(r) from RetiradaCesta r where r.voluntarioEntrega.id = :id and r.cancelada = false " +
                                "and r.mesReferencia between :inicio and :fim")
                .setParameter("id", voluntarioId).setParameter("inicio", inicio).setParameter("fim", fim).getSingleResult());
    }

    public long contarEntregasTotal(Long voluntarioId) {
        return numero(entityManager.createQuery(
                        "select count(r) from RetiradaCesta r where r.voluntarioEntrega.id = :id and r.cancelada = false")
                .setParameter("id", voluntarioId).getSingleResult());
    }

    // ---- Perfil social (só beneficiários ativos) ----

    public double mediaPessoasResidencia() {
        return mediaOuZero("select avg(b.pessoasResidencia) from Beneficiario b where b.ativo = true and b.pessoasResidencia is not null");
    }

    public double mediaQuantidadeFilhos() {
        return mediaOuZero("select avg(b.quantidadeFilhos) from Beneficiario b where b.ativo = true and b.quantidadeFilhos is not null");
    }

    public long contarComFilhos() {
        return numero(entityManager.createQuery(
                "select count(b) from Beneficiario b where b.ativo = true and b.temFilhos = br.com.imgazin.beneficios.domain.RespostaSimNao.SIM")
                .getSingleResult());
    }

    public long contarFilhosComDeficiencia() {
        return numero(entityManager.createQuery(
                "select count(b) from Beneficiario b where b.ativo = true and b.filhosComDeficiencia = br.com.imgazin.beneficios.domain.RespostaSimNao.SIM")
                .getSingleResult());
    }

    public long contarFamiliarComDeficiencia() {
        return numero(entityManager.createQuery(
                "select count(b) from Beneficiario b where b.ativo = true and b.familiarComDeficiencia = br.com.imgazin.beneficios.domain.RespostaSimNao.SIM")
                .getSingleResult());
    }

    public long contarSemTrabalho() {
        return numero(entityManager.createQuery(
                "select count(b) from Beneficiario b where b.ativo = true and b.temTrabalho = br.com.imgazin.beneficios.domain.RespostaSimNao.NAO")
                .getSingleResult());
    }

    private double mediaOuZero(String jpql) {
        Object resultado = entityManager.createQuery(jpql).getSingleResult();
        return resultado == null ? 0.0 : ((Number) resultado).doubleValue();
    }

    @SuppressWarnings("unchecked")
    public List<Integer> idadesBeneficiariosAtivos() {
        List<LocalDate> nascimentos = entityManager.createQuery(
                        "select b.dataNascimento from Beneficiario b where b.ativo = true and b.dataNascimento is not null")
                .getResultList();
        LocalDate hoje = LocalDate.now();
        return nascimentos.stream().map(data -> java.time.Period.between(data, hoje).getYears()).toList();
    }

    // ---- Confirmações (conformidade) ----

    @SuppressWarnings("unchecked")
    public List<Object[]> confirmacoesNoPeriodo(LocalDateTime inicio, LocalDateTime fim, String codigo) {
        StringBuilder jpql = new StringBuilder(
                "select c.confirmadoEm, c.codigoAviso, c.severidade, c.retirada.id, " +
                        "c.retirada.beneficiario.nome, c.confirmadoPor, c.justificativa " +
                        "from RetiradaConfirmacao c where c.confirmadoEm between :inicio and :fim");
        if (codigo != null && !codigo.isBlank()) {
            jpql.append(" and c.codigoAviso = :codigo");
        }
        jpql.append(" order by c.confirmadoEm desc");

        Query query = entityManager.createQuery(jpql.toString())
                .setParameter("inicio", inicio).setParameter("fim", fim);
        if (codigo != null && !codigo.isBlank()) {
            query.setParameter("codigo", codigo);
        }
        return query.getResultList();
    }

    // ---- helpers ----

    private long numero(Object valor) {
        if (valor == null) {
            return 0L;
        }
        if (valor instanceof BigDecimal bd) {
            return bd.longValue();
        }
        return ((Number) valor).longValue();
    }
}
