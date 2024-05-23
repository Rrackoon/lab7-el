package ru.itmo.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itmo.common.commands.base.Command;
import ru.itmo.common.commands.base.CommandShallow;
import ru.itmo.common.commands.base.Response;
import ru.itmo.common.exceptions.CommandIOException;
import ru.itmo.common.managers.CommandManager;
import ru.itmo.common.models.StudyGroup;
import ru.itmo.common.utility.IOProvider;
import ru.itmo.common.utility.SGParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Scanner;

public class Console {
    // Логгер для записи логов
    private static final Logger logger = LoggerFactory.getLogger(Console.class);
    // Флаг активности консоли
    private static boolean active;
    // Объекты для отправки и получения UDP сообщений
    private static UDPSender sender;
    private static UDPReader reader;
    // Менеджер команд
    private final CommandManager commandManager;
    // Провайдер ввода-вывода
    private final IOProvider provider;
    // Сканнер для считывания ввода с консоли
    private final Scanner scanner;
    // Стек для хранения истории команд
    private final LinkedList<String> commandsStack;
    // Флаг авторизации пользователя
    private boolean authorized;
    // Логин и пароль пользователя
    private String login;
    private String password;

    // Конструктор класса Console
    public Console(CommandManager commandManager, UDPSender sender, UDPReader reader, IOProvider provider) {
        this.scanner = new Scanner(System.in);
        Console.active = true;
        this.commandManager = commandManager;
        this.provider = provider;
        this.commandsStack = new LinkedList<>();
        Console.sender = sender;
        Console.reader = reader;
    }

    // Метод для остановки консоли
    public static void stop() {
        active = false;
        logger.info("Console stopped.");
    }

    // Метод для выполнения команды
    public static void executeCommand(CommandShallow shallow, IOProvider provider, CommandManager commandManager) throws IOException {
        // Проверка, содержит ли команда 'add' или является ли она 'update'
        if (shallow.getCommand().contains("add") || shallow.getCommand().equals("update")) {
            try {
                // Создание объекта StudyGroup через парсер
                StudyGroup sg = new SGParser(provider.getScanner(), provider.getPrinter(), provider.isPrintValue()).parseStudyGroup();
                sg.setLogin(shallow.getLogin());
                shallow.setStudyGroup(sg);
            } catch (Exception e) {
                logger.error("Error creating StudyGroup: {}", e.getMessage());
                System.out.println(e.getMessage());
                return;
            }
        }

        // Сериализация команды и отправка её через UDP
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(shallow);
            oos.flush();

            byte[] arr = baos.toByteArray();
            logger.debug("Sending command {}: {} bytes", shallow.getCommand(), arr.length);
            sender.send(arr);

            // Чтение ответа от сервера
            Response response = reader.readResponse();
            handleResponse(response);

        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error executing command: {}", e.getMessage());
            System.out.println("Ошибка при выполнении команды: " + e.getMessage());
        }
    }

    // Метод для обработки ответа от сервера
    private static void handleResponse(Response response) throws IOException, ClassNotFoundException {
        String responseMessage = new String(response.getMessage().getBytes(), Charset.defaultCharset());

        // Если ответ состоит из нескольких частей
        if (response.getResponseNumber() > 1) {
            responseMessage = handleMultipleResponses(response);
        }

        // Если команда "exit", остановка консоли
        if ("exit".equals(responseMessage)) {
            Console.stop();
        }
        System.out.println(responseMessage);
    }

    // Метод для обработки множественных ответов
    private static String handleMultipleResponses(Response initialResponse) throws IOException, ClassNotFoundException {
        ArrayList<Response> responses = new ArrayList<>();
        responses.add(initialResponse);
        int rcount = 1;

        // Чтение всех частей ответа
        while (rcount < initialResponse.getResponseNumber()) {
            try {
                responses.add(reader.readResponse());
                rcount++;
            } catch (SocketTimeoutException e) {
                logger.warn("Socket timed out while waiting for response part.");
                System.out.println("ReadSocket Timeout");
                break;
            }
        }

        // Сборка частей ответа в один
        if (rcount == initialResponse.getResponseNumber()) {
            responses.sort(Comparator.comparingInt(Response::getResponseCount));
            int messageLength = responses.stream().mapToInt(r -> r.getMessage().length()).sum();
            ByteBuffer mbb = ByteBuffer.allocate(messageLength);
            responses.forEach(r -> mbb.put(r.getMessage().getBytes()));
            mbb.rewind();
            return new String(mbb.array(), Charset.defaultCharset());
        } else {
            logger.error("Error in receiving all parts of the response.");
            System.out.println("Ошибка приёма-передачи, повторите.");
            return "";
        }
    }

    // Метод для получения провайдера
    public IOProvider getProvider() {
        return provider;
    }

    // Метод для проверки активности консоли
    public boolean isActive() {
        return active;
    }

    // Метод для получения сканера
    public Scanner getScanner() {
        return scanner;
    }

    // Метод для запуска консоли
    public void start(UDPConnector connector) {
        logger.info("Starting console and connecting to server...");
        if (connector.connect()) {
            sender = new UDPSender(connector.getDatagramSocket(), connector.getServerAddress(), connector.getServerPort());
            reader = new UDPReader(connector.getDatagramSocket());
            while (Console.active) {
                readCommand(provider);
            }
        } else {
            logger.error("Failed to connect to the server.");
        }
    }

    // Метод для печати строки в консоль
    public void print(String line) {
        if (line != null) {
            System.out.print(line);
        }
    }

    // Метод для печати строки с новой строки в консоль
    public void println(String line) {
        if (line != null) {
            System.out.println(line);
        } else {
            System.out.println();
        }
    }

    // Метод для чтения и выполнения команды с консоли
    public void readCommand(IOProvider provider) {
        logger.debug("Awaiting command input...");
        System.out.print("Введите команду (или help): ");
        String[] com;
        com = scanner.nextLine().split("\\s");
        if (com.length == 0 || com[0].isEmpty()) {
            logger.warn("Empty command entered.");
            System.out.println("Команда не должна быть пустой");
            return;
        }

        String commandName = com[0];
        String arg = String.join(" ", com.length > 1 ? com[1] : "");

        try {
            Command command = commandManager.getCommand(commandName);
            if (command == null) {
                logger.warn("No such command: {}", commandName);
                System.out.println("Нет такой команды");
                return;
            }
            if ("login".equals(command.getName()) || "register".equals(command.getName())) {
                System.out.print("Введите логин: ");
                login = scanner.nextLine().trim();
                System.out.print("Введите пароль: ");
                password = scanner.nextLine().trim();
            }
            CommandShallow shallow = new CommandShallow(command.getName(), arg, login, password);
            if (!argCheck(commandName, arg)) {
                return;
            }

            if (command.getName().contains("execute_script")) {
                command.execute(arg);
                return;
            }

            executeCommand(shallow, provider, commandManager);

        } catch (CommandIOException e) {
            logger.error("CommandIOException occurred: {}", e.getMessage());
            System.out.println("Введена несуществующая команда");
        } catch (Exception e) {
            logger.error("Exception occurred: {}", e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    // Метод для проверки аргументов команды
    private boolean argCheck(String name, String arg) {
        if ((name.equals("execute_script") || name.equals("count_less_than_group_admin")) && !arg.isEmpty()) {
            return true;
        } else if (name.equals("update") || name.equals("remove_by_id")) {
            try {
                Long.parseLong(arg);
            } catch (NumberFormatException e) {
                logger.warn("Argument value is not a long: {}", arg);
                System.out.println("Значение аргумента не long");
                return false;
            }
            return true;
        }
        return true;
    }
}
