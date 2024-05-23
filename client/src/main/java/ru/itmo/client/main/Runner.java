package ru.itmo.client.main;

import ru.itmo.client.core.Console;
import ru.itmo.client.core.UDPConnector;
import ru.itmo.client.core.UDPReader;
import ru.itmo.client.core.UDPSender;
import ru.itmo.common.commands.*;
import ru.itmo.common.commands.base.Command;
import ru.itmo.common.exceptions.CommandIOException;
import ru.itmo.common.interfaces.Printer;
import ru.itmo.common.managers.CommandManager;
import ru.itmo.common.utility.IOProvider;
import ru.itmo.common.utility.StandardPrinter;

import java.util.Scanner;

public class Runner {

    private IOProvider provider;
    private CommandManager commandManager;
    private UDPConnector connector;
    private Console console;

    public Runner() {
        this.provider = initializeIOProvider();
        this.commandManager = initializeCommandManager(provider);
        this.connector = initializeUDPConnector();
        this.console = initializeConsole(commandManager, provider, connector);
    }

    private IOProvider initializeIOProvider() {
        Scanner scanner = new Scanner(System.in);
        Printer printer = new StandardPrinter();
        return new IOProvider(scanner, printer);
    }

    private CommandManager initializeCommandManager(IOProvider provider) {
        CommandManager commandManager = new CommandManager(provider);
        String[] commandNames = {"help", "info", "show", "add", "update", "remove_by_id", "clear", "save",
                "execute_script", "exit", "add_if_min", "count_less_than_group_admin",
                "print_asceding", "remove_first", "login", "register"};
        Command[] commands = {new Help(), new Info(), new Show(), new Add(), new Update(),
                new RemoveById(), new Clear(), new ExecuteScript(), new Exit(),
                new AddIfMin(), new CountLesAdminName(), new Update(), new PrintAsceding(), new RemoveFirst(), new LogIn(), new Register()};

        for (int i = 0; i < commands.length; i++) {
            try {
                commandManager.createCommand(commandNames[i], commands[i]);
            } catch (CommandIOException e) {
                provider.getPrinter().print(e.getMessage());
            }
        }
        return commandManager;
    }

    private UDPConnector initializeUDPConnector() {
        return new UDPConnector("localhost", 9999);
    }

    private Console initializeConsole(CommandManager commandManager, IOProvider provider, UDPConnector connector) {
        UDPSender sender = new UDPSender(connector.getDatagramSocket(), connector.getServerAddress(), connector.getServerPort());
        UDPReader reader = new UDPReader(connector.getDatagramSocket());
        return new Console(commandManager, sender, reader, provider);
    }

    public void start() {
        console.start(connector);
    }
}
