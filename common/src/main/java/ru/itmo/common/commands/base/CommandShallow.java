package ru.itmo.common.commands.base;


import lombok.Setter;
import ru.itmo.common.models.StudyGroup;

import java.io.Serial;
import java.io.Serializable;

@Setter
public class CommandShallow implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String command;
    private String args;
    private StudyGroup studyGroup;
    private String login;
    private String password;

    public CommandShallow() {
        this.command = null;
        this.args = null;
        this.studyGroup = null;
        this.login = null;
        this.password = null;
    }

    public CommandShallow(String command, String args) {
        this.command = command;
        this.args = args;
        this.studyGroup = null;
        this.login = login;
        this.password = password;
    }

    public CommandShallow(String command, String args, String login, String password) {
        this.command = command;
        this.args = args;
        this.studyGroup = null;
        this.login = login;
        this.password = password;
    }


    public String getCommand() {
        return command;
    }

    public String getArguments() {
        return args;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public StudyGroup getStudyGroup() {
        return studyGroup;
    }

    public void setStudyGroup(StudyGroup sg) {
        this.studyGroup = sg;
    }
}
