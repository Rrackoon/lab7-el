package ru.itmo.common.commands;

import ru.itmo.common.commands.base.Command;
import ru.itmo.common.commands.base.CommandContext;
import ru.itmo.common.commands.base.Response;
import ru.itmo.common.managers.CollectionManager;

public class Clear extends Command {
    public Clear() {
        super("clear", "очистить коллекцию");
    }

    @Override
    public Response[] execute(CommandContext context) {
        CollectionManager collectionManager = context.getCollectionManager();
        String[] response = new String[1];

        try {
            collectionManager.clear(context.getLogin());
            response[0] = "Коллекция очищена";
        } catch (Exception e) {
            response[0] = "Ошибка при очистке коллекции: " + e.getMessage();
        }

        return Response.createResponses(response);
    }
}
