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

    // Поля для провайдера ввода-вывода, менеджера команд, соединителя UDP и консоли
    private IOProvider provider;
    private CommandManager commandManager;
    private UDPConnector connector;
    private Console console;

    // Конструктор Runner, инициализирующий все компоненты
    public Runner() {
        this.provider = initializeIOProvider(); // Инициализация провайдера ввода-вывода
        this.commandManager = initializeCommandManager(provider); // Инициализация менеджера команд
        this.connector = initializeUDPConnector(); // Инициализация соединителя UDP
        this.console = initializeConsole(commandManager, provider, connector); // Инициализация консоли
    }

    // Метод для инициализации провайдера ввода-вывода
    private IOProvider initializeIOProvider() {
        Scanner scanner = new Scanner(System.in); // Создание сканера для чтения ввода
        Printer printer = new StandardPrinter(); // Создание принтера для вывода
        return new IOProvider(scanner, printer); // Возвращение нового IOProvider с заданными сканером и принтером
    }

    // Метод для инициализации менеджера команд
    private CommandManager initializeCommandManager(IOProvider provider) {
        CommandManager commandManager = new CommandManager(provider); // Создание нового менеджера команд
        // Массивы с именами команд и соответствующими объектами команд
        String[] commandNames = {"help", "info", "show", "add", "update", "remove_by_id", "clear", "save",
                "execute_script", "exit", "add_if_min", "count_less_than_group_admin",
                "print_asceding", "remove_first", "login", "register"};
        Command[] commands = {new Help(), new Info(), new Show(), new Add(), new Update(),
                new RemoveById(), new Clear(), new ExecuteScript(), new Exit(),
                new AddIfMin(), new CountLesAdminName(), new PrintAsceding(), new RemoveFirst(), new LogIn(), new Register()};

        // Добавление каждой команды в менеджер команд
        for (int i = 0; i < commands.length; i++) {
            try {
                commandManager.createCommand(commandNames[i], commands[i]);
            } catch (CommandIOException e) {
                provider.getPrinter().print(e.getMessage()); // Вывод сообщения об ошибке, если команда не добавлена
            }
        }
        return commandManager; // Возвращение инициализированного менеджера команд
    }

    // Метод для инициализации соединителя UDP
    private UDPConnector initializeUDPConnector() {
        return new UDPConnector("localhost", 9999); // Возвращение нового UDPConnector с заданным хостом и портом
    }

    // Метод для инициализации консоли
    private Console initializeConsole(CommandManager commandManager, IOProvider provider, UDPConnector connector) {
        UDPSender sender = new UDPSender(connector.getDatagramSocket(), connector.getServerAddress(), connector.getServerPort()); // Создание отправителя UDP
        UDPReader reader = new UDPReader(connector.getDatagramSocket()); // Создание читателя UDP
        return new Console(commandManager, sender, reader, provider); // Возвращение новой консоли с заданными компонентами
    }

    // Метод для запуска консоли
    public void start() {
        console.start(connector); // Запуск консоли с соединителем
    }
}
