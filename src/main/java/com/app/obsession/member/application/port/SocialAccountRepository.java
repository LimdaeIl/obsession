package com.app.obsession.member.application.port;

import com.app.obsession.member.domain.SocialAccount;
import com.app.obsession.member.domain.SocialProvider;
import java.util.Optional;

public interface SocialAccountRepository {

    Optional<SocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);

    Optional<SocialAccount> findByMemberId(Long memberId);

    SocialAccount save(SocialAccount socialAccount);
}
