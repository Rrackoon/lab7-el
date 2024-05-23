package ru.itmo.common.commands.base;

import ru.itmo.common.managers.CollectionManager;
import ru.itmo.common.models.StudyGroup;
import ru.itmo.common.utility.IOProvider;

public class CommandContext {
    private String args;
    private StudyGroup studyGroup;
    private CollectionManager collectionManager;
    private String login;
    private String password;
    private IOProvider ioProvider;

    public CommandContext(String args, StudyGroup studyGroup,
                          CollectionManager collectionManager,
                          String login, String password) {
        this.args = args;
        this.studyGroup = studyGroup;
        this.collectionManager = collectionManager;
        this.login = login;
        this.password = password;
    }

    // Геттеры для всех полей

    public String getArgs() {
        return args;
    }

    public StudyGroup getStudyGroup() {
        return studyGroup;
    }

    public CollectionManager getCollectionManager() {
        return collectionManager;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public IOProvider getProvider() {
        return ioProvider;
    }
}
