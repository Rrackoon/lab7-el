package ru.itmo.common.commands;

import ru.itmo.common.commands.base.Command;
import ru.itmo.common.commands.base.CommandContext;
import ru.itmo.common.commands.base.Response;
import ru.itmo.common.managers.CollectionManager;
import ru.itmo.common.models.StudyGroup;

public class Info extends Command {
    public Info() {
        super("info", "вывести информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)");
    }

    @Override
    public Response[] execute(CommandContext context) {
        CollectionManager<StudyGroup> collectionManager = context.getCollectionManager();

        String[] response = new String[3];
        response[0] = "Коллекция: " + collectionManager.getCollection().getClass().getName();
        response[1] = "Количество групп: " + collectionManager.getCollection().size();
        response[2] = "Дата создания: " + collectionManager.getCreatedAt();

        return Response.createResponses(response);
    }
}
