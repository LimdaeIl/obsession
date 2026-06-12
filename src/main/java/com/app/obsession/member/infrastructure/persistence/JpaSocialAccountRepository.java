package com.app.obsession.member.infrastructure.persistence;

import com.app.obsession.member.domain.SocialAccount;
import com.app.obsession.member.domain.SocialProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);

    Optional<SocialAccount> findByMemberId(Long memberId);

}
