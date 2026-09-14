package br.com.imgazin.beneficios.repository;

import br.com.imgazin.beneficios.domain.Beneficiario;
import br.com.imgazin.beneficios.domain.StatusBeneficiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BeneficiarioRepository extends JpaRepository<Beneficiario, Long>, JpaSpecificationExecutor<Beneficiario> {

    Optional<Beneficiario> findByCpf(String cpf);

    /** Detecta outros cadastros ativos no mesmo endereço (mesma chave normalizada). */
    List<Beneficiario> findByEnderecoChaveResidenciaAndAtivoTrue(String chaveResidencia);

    /** Detecta outros cadastros ativos com a mesma mãe informada (possíveis irmãos). */
    List<Beneficiario> findByNomeMaeIgnoreCaseAndAtivoTrue(String nomeMae);

    /** Aviso CPF_JA_E_CONJUGE: esse CPF já está cadastrado como cônjuge de outro beneficiário ativo. */
    List<Beneficiario> findByCpfConjugeAndAtivoTrue(String cpfConjuge);

    /** Mesma ideia, pelo nome do cônjuge (quando o CPF do cônjuge não foi informado). */
    List<Beneficiario> findByNomeConjugeIgnoreCaseAndAtivoTrue(String nomeConjuge);

    /** Aviso TELEFONE_DUPLICADO. */
    List<Beneficiario> findByTelefoneAndAtivoTrue(String telefone);

    /** Base para o aviso NOME_SIMILAR (comparação feita em Java, ver TextoUtils). */
    List<Beneficiario> findByAtivoTrue();

    /** Job de virada de mês: quem está com RETIRADO_MES_ATUAL para voltar a APTO_MES_ATUAL. */
    List<Beneficiario> findByStatus(StatusBeneficiario status);

    /** Job de virada de mês: quem venceu o prazo do benefício e ainda não está marcado como tal. */
    List<Beneficiario> findByAtivoTrueAndPrazoFinalBeneficioBeforeAndStatusNot(LocalDate data, StatusBeneficiario statusAtual);

    /** Apadrinhados atuais de um voluntário (Etapa 4). */
    List<Beneficiario> findByPadrinhoId(Long padrinhoId);
}
