package edu.unialfa.institutoMario.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long>, JpaSpecificationExecutor<LogAuditoria> {

    /** Lista dos nomes de entidade já registrados, para alimentar o filtro da tela de consulta. */
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT l.entidade FROM LogAuditoria l ORDER BY l.entidade")
    java.util.List<String> listarEntidadesDistintas();
}
