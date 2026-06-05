package com.app.obsession.member.domain;

import com.app.obsession.global.entity.BaseAuditEntity;
import com.app.obsession.member.exception.MemberErrorCode;
import com.app.obsession.member.exception.MemberException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "v1_social_accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_social_provider_provider_id",
                        columnNames = {"social_provider", "social_provider_id"}
                ),
                @UniqueConstraint(
                        name = "uk_social_member_provider",
                        columnNames = {"member_id", "social_provider"}
                )
        }
)
@Entity
public class SocialAccount extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_account_id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false, length = 20)
    private SocialProvider provider;

    @Column(name = "social_provider_id", nullable = false, length = 100)
    private String providerId;

    private SocialAccount(Long memberId, SocialProvider provider, String providerId) {
        this.memberId = memberId;
        this.provider = provider;
        this.providerId = providerId;
    }

    public static SocialAccount create(Long memberId, SocialProvider provider, String providerId) {
        validate(memberId, provider, providerId);
        return new SocialAccount(memberId, provider, providerId);
    }

    public void updateProviderId(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            throw new MemberException(MemberErrorCode.INVALID_SOCIAL_PROVIDER_ID);
        }
        this.providerId = providerId;
    }

    public boolean isProvider(SocialProvider provider) {
        return this.provider == provider;
    }

    private static void validate(Long memberId, SocialProvider provider, String providerId) {
        if (memberId == null) {
            throw new MemberException(MemberErrorCode.INVALID_SOCIAL_MEMBER_ID);
        }

        if (provider == null) {
            throw new MemberException(MemberErrorCode.INVALID_SOCIAL_PROVIDER);
        }

        if (providerId == null || providerId.isBlank()) {
            throw new MemberException(MemberErrorCode.INVALID_SOCIAL_PROVIDER_ID);
        }
    }
}
