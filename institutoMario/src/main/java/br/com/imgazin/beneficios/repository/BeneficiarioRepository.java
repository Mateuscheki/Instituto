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

    List<Beneficiario> findByEnderecoChaveResidenciaAndAtivoTrue(String chaveResidencia);

    List<Beneficiario> findByNomeMaeIgnoreCaseAndAtivoTrue(String nomeMae);

    List<Beneficiario> findByCpfConjugeAndAtivoTrue(String cpfConjuge);

    List<Beneficiario> findByNomeConjugeIgnoreCaseAndAtivoTrue(String nomeConjuge);

    List<Beneficiario> findByTelefoneAndAtivoTrue(String telefone);

    List<Beneficiario> findByAtivoTrue();

    List<Beneficiario> findByStatus(StatusBeneficiario status);

    List<Beneficiario> findByAtivoTrueAndPrazoFinalBeneficioBeforeAndStatusNot(LocalDate data, StatusBeneficiario statusAtual);

    List<Beneficiario> findByPadrinhoId(Long padrinhoId);
}
