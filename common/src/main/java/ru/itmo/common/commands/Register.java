package ru.itmo.common.commands;

import ru.itmo.common.commands.base.Command;
import ru.itmo.common.commands.base.CommandContext;
import ru.itmo.common.commands.base.Response;
import ru.itmo.common.interfaces.Accessible;

public class Register extends Command {
    private Accessible connector;

    public Register() {
        super("register", "зарегистрироваться в системе");
    }

    public Register(Accessible connector) {
        super("register", "зарегистрироваться в системе");
        this.connector = connector;
    }

    @Override
    public Response[] execute(CommandContext context) {
        String args = context.getArgs();
        String[] response = new String[1];

        try {
            if (context.getLogin() == null || context.getLogin().isEmpty() ||
                    context.getPassword() == null || context.getPassword().isEmpty()) {
                response[0] = "Некорректный логин или пароль.";
            } else {
                response[0] = connector.register(context.getLogin(), context.getPassword());
            }
        } catch (Exception e) {
            response[0] = "Ошибка при выполнении команды: " + e.getMessage();
        }

        return Response.createResponses(response);
    }
}
