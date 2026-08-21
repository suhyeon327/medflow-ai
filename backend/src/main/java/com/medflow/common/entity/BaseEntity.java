package com.medflow.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass   // 부모 클래스의 필드 매핑 정보를 자식 Entity가 상속받도록 함
@EntityListeners(AuditingEntityListener.class)   // JPA 엔티티의 변경 이벤트를 감지하여 자동으로 특정 작업을 수행
public abstract class BaseEntity {

    @CreatedDate   // 엔티티가 처음 생성될 때 자동으로 시간 기록
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate   // 엔티티가 수정될 때 자동으로 시간 기록
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 데이터를 진짜 삭제하지 않고 삭제된 것처럼 표시
    public void softDelete() {
        if (deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
        }
    }
}
