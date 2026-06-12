package com.app.obsession.member.infrastructure.persistence;

import com.app.obsession.member.application.port.SocialAccountRepository;
import com.app.obsession.member.domain.SocialAccount;
import com.app.obsession.member.domain.SocialProvider;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class SocialAccountRepositoryImpl implements SocialAccountRepository {

    private final JpaSocialAccountRepository jpaSocialAccountRepository;


    @Override
    public Optional<SocialAccount> findByProviderAndProviderId(SocialProvider provider,
            String providerId) {
        return jpaSocialAccountRepository.findByProviderAndProviderId(provider, providerId);
    }

    @Override
    public Optional<SocialAccount> findByMemberId(Long memberId) {
        return jpaSocialAccountRepository.findByMemberId(memberId);
    }

    @Override
    public SocialAccount save(SocialAccount socialAccount) {
        return jpaSocialAccountRepository.save(socialAccount);
    }
}
