package br.com.imgazin.beneficios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditoria {

    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @CreatedBy
    @Column(name = "criado_por", nullable = false, updatable = false, length = 120)
    private String criadoPor;

    @LastModifiedDate
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @LastModifiedBy
    @Column(name = "atualizado_por", length = 120)
    private String atualizadoPor;
}
