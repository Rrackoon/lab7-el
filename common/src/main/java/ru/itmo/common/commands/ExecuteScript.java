package ru.itmo.common.commands;

import ru.itmo.common.commands.base.Command;
import ru.itmo.common.commands.base.CommandContext;
import ru.itmo.common.commands.base.Response;
import ru.itmo.common.managers.CommandManager;
import ru.itmo.common.parser.CommandParser;
import ru.itmo.common.utility.IOProvider;
import ru.itmo.common.utility.StandardPrinter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class ExecuteScript extends Command {
    private final int recDepth;

    public ExecuteScript() {
        super("execute_script", "считать и исполнить скрипт из указанного файла");
        this.recDepth = 1;
    }

    public ExecuteScript(int recDepth) {
        super("execute_script", "считать и исполнить скрипт из указанного файла");
        this.recDepth = recDepth;
    }

    @Override
    public Response[] execute(CommandContext context) {
        return new Response[0];
    }

    @Override
    public void execute(String args) {
        if (!validateArgs(args, 2)) {
            return;
        }
        String fileName = args;
        try (FileReader fileReader = new FileReader(fileName)) {//открывается файл + создается экземпляр
            var provider = new IOProvider(new Scanner(fileReader), new StandardPrinter(), true);
            var commandManager = new CommandManager(provider);
            var commandParser = new CommandParser(commandManager, provider, recDepth + 1);
            commandParser.run();
        } catch (FileNotFoundException e) {
            System.out.println("File not found or access denied (read).");
            System.out.println("Нет такого файла");
            return;
        } catch (IOException e) {
            System.out.println("Something went wrong while reading.");
        }
    }
}
