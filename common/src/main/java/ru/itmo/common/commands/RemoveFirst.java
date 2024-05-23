package ru.itmo.common.commands;

import ru.itmo.common.commands.base.Command;
import ru.itmo.common.commands.base.CommandContext;
import ru.itmo.common.commands.base.Response;
import ru.itmo.common.managers.CollectionManager;
import ru.itmo.common.models.StudyGroup;

public class RemoveFirst extends Command {
    public RemoveFirst() {
        super("remove_first", "удалить первый элемент из коллекции");
    }

    @Override
    public Response[] execute(CommandContext context) {
        CollectionManager<StudyGroup> collectionManager = context.getCollectionManager();
        String[] response = new String[1];

        try {
            if (collectionManager.getCollection().isEmpty()) {
                response[0] = "Коллекция пуста, удаление невозможно.";
            } else {
                StudyGroup firstElement = collectionManager.getCollection().get(0);
                collectionManager.removeById(firstElement.getId(), context.getLogin());
                response[0] = "Первый элемент удален.";
            }
        } catch (Exception e) {
            response[0] = "Ошибка при удалении первого элемента: " + e.getMessage();
        }

        return Response.createResponses(response);
    }
}
