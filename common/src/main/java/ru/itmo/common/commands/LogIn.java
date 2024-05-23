package ru.itmo.common.commands;

import ru.itmo.common.commands.base.Command;
import ru.itmo.common.commands.base.CommandContext;
import ru.itmo.common.commands.base.Response;
import ru.itmo.common.interfaces.Accessible;

public class LogIn extends Command {
    private Accessible connector;

    public LogIn() {
        super("login", "войти в систему");
    }

    public LogIn(Accessible connector) {
        this();
        this.connector = connector;
    }

    @Override
    public Response[] execute(CommandContext context) {
        String[] response = new String[1];

        try {
            if (context.getLogin() == null || context.getLogin().isEmpty() ||
                    context.getPassword() == null || context.getPassword().isEmpty()) {
                response[0] = "Некорректный логин или пароль.";
            } else {
                response[0] = connector.signIn(context.getLogin(), context.getPassword());
            }
        } catch (Exception e) {
            response[0] = "Ошибка при выполнении команды: " + e.getMessage();
        }

        return Response.createResponses(response);
    }
}
