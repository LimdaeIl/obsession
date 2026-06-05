package com.app.obsession.member.application.command;

public record LoginCommand(
        String email,
        String password
) {
}
