package com.app.obsession.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity extends BaseTimeEntity {

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 100)
    private Long updatedBy;
}
