package com.app.obsession.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class CompanyInfo {

    @Column(name = "company_name", length = 50)
    private String companyName;

    @Column(name = "brn", length = 50)
    private String brn;

    public CompanyInfo(String companyName, String brn) {
        validate(companyName, brn);
        this.companyName = companyName;
        this.brn = brn;
    }

    private void validate(String companyName, String brn) {
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("회사명은 필수입니다.");
        }

        if (brn == null || brn.isBlank()) {
            throw new IllegalArgumentException("사업자등록번호는 필수입니다.");
        }
    }
}