package com.app.obsession.member.infrastructure.persistence;

import com.app.obsession.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMemberRepository extends JpaRepository<Member, Long> {

    boolean existsByProfileEmail(String email);

    Optional<Member> findByProfileEmail(String email);
}
