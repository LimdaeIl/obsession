package com.app.obsession.member.domain;

import com.app.obsession.global.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "v1_members")
@Entity
public class Member extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", updatable = false, nullable = false)
    private Long id;

    @Embedded
    private Profile profile;

    @Embedded
    private Password password;

    @Embedded
    private CompanyInfo companyInfo;

    @Column(name = "member_role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Column(name = "member_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    private Member(Profile profile, Password password, CompanyInfo companyInfo, MemberRole role) {
        this.profile = profile;
        this.password = password;
        this.companyInfo = companyInfo;
        this.role = role;
        this.status = MemberStatus.ACTIVE;
    }

    public static Member createCustomer(Profile profile, Password password) {
        return new Member(profile, password, null, MemberRole.CUSTOMER);
    }

    public static Member createBusiness(Profile profile, Password password, CompanyInfo companyInfo) {
        return new Member(profile, password, companyInfo, MemberRole.BUSINESS);
    }

    public void changeProfile(String name, String phone) {
        this.profile = this.profile.change(name, phone);
    }

    public void changePassword(String encodedPassword) {
        this.password = Password.encoded(encodedPassword);
    }

    public void deactivate() {
        this.status = MemberStatus.INACTIVE;
    }

    public void activate() {
        this.status = MemberStatus.ACTIVE;
    }

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
    }

    public boolean hasPassword() {
        return this.password.exists();
    }

    public boolean isSocialOnlyMember() {
        return !hasPassword();
    }

    public boolean isWithdrawn() {
        return this.status == MemberStatus.WITHDRAWN;
    }

    public boolean isBusiness() {
        return this.role == MemberRole.BUSINESS;
    }
}