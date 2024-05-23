package ru.itmo.common.commands;

import ru.itmo.common.commands.base.Command;
import ru.itmo.common.commands.base.CommandContext;
import ru.itmo.common.commands.base.Response;
import ru.itmo.common.managers.CollectionManager;
import ru.itmo.common.models.StudyGroup;

import java.util.Arrays;

public class Add extends Command {

    public Add() {
        super("add", "добавить новый элемент в коллекцию");
    }

    @Override
    public Response[] execute(CommandContext context) {
        StudyGroup studyGroup = context.getStudyGroup();
        CollectionManager<StudyGroup> collectionManager = context.getCollectionManager();

        String[] pids = collectionManager.getCollection().stream()
                .map(dr -> dr.getGroupAdmin().getPassportID())
                .toArray(String[]::new);
        String[] response = new String[1];
        if (Arrays.asList(pids).contains(studyGroup.getGroupAdmin().getPassportID())) {
            response[0] = "Нарушена уникальность passportID: " + studyGroup.getGroupAdmin().getPassportID();
        } else if (!studyGroup.validate()) {
            response[0] = "Группа не валидна";
            System.out.println(studyGroup);
        } else {
            try {
                collectionManager.push(studyGroup);
                response[0] = "Добавлена группа: " + studyGroup.getName();
            } catch (Exception e) {
                response[0] = "Ошибка при добавлении группы: " + e.getMessage();
            }
        }
        return Response.createResponses(response);
    }
}
