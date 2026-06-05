package com.app.obsession.member.application.port;

public interface PasswordEncryptor {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
