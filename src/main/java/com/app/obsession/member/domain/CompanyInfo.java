package com.app.obsession.member.domain;

import com.app.obsession.member.exception.MemberErrorCode;
import com.app.obsession.member.exception.MemberException;
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
            throw new MemberException(MemberErrorCode.INVALID_COMPANY_NAME);
        }

        if (brn == null || brn.isBlank()) {
            throw new MemberException(MemberErrorCode.INVALID_COMPANY_BRN);
        }
    }
}
