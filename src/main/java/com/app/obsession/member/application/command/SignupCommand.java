package com.app.obsession.member.application.command;

public record SignupCommand(
        String email,
        String password,
        String name,
        String phone
) {
}
