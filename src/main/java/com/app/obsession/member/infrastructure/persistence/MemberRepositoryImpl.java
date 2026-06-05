package com.app.obsession.member.infrastructure.persistence;

import com.app.obsession.member.application.port.MemberRepository;
import com.app.obsession.member.domain.Member;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final JpaMemberRepository jpaMemberRepository;

    @Override
    public boolean existsByEmail(String email) {
        return jpaMemberRepository.existsByProfileEmail(email);
    }

    @Override
    public Member save(Member member) {
        return jpaMemberRepository.save(member);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return jpaMemberRepository.findByProfileEmail(email);
    }

    @Override
    public Optional<Member> findById(Long memberId) {
        return jpaMemberRepository.findById(memberId);
    }
}
