package com.app.obsession.global.security.auth;

import com.app.obsession.member.domain.Member;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long memberId;
    private final String email;
    private final String password;
    private final String role;
    private final boolean enabled;

    public CustomUserDetails(Member member) {
        this.memberId = member.getId();
        this.email = member.getProfile().getEmail();
        this.password = member.getPassword().getValue();
        this.role = member.getRole().name();
        this.enabled = !member.isWithdrawn();
    }

    public CustomUserDetails(Long memberId, String role) {
        this.memberId = memberId;
        this.email = null;
        this.password = null;
        this.role = role;
        this.enabled = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return memberId.toString();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
