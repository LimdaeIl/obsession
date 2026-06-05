package com.app.obsession.member.application.port;

import com.app.obsession.member.domain.Member;
import java.util.Optional;

public interface MemberRepository {

    boolean existsByEmail(String email);

    Member save(Member member);

    Optional<Member> findByEmail(String email);
}
