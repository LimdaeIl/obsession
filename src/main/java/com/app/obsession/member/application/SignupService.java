package com.app.obsession.member.application;

import static com.app.obsession.member.exception.MemberErrorCode.DUPLICATE_EMAIL;

import com.app.obsession.member.application.command.SignupCommand;
import com.app.obsession.member.application.port.MemberRepository;
import com.app.obsession.member.application.port.PasswordEncoder;
import com.app.obsession.member.domain.Member;
import com.app.obsession.member.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signup(SignupCommand command) {

        if (memberRepository.existsByEmail(command.email())) {
            throw new MemberException(DUPLICATE_EMAIL, command.email());
        }

        String encodedPassword = passwordEncoder.encode(command.password());

        Member member = Member.createCustomer(
                command.name(),
                command.email(),
                command.phone(),
                encodedPassword
        );

        return memberRepository.save(member).getId();
    }
}

