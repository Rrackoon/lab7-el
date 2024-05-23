package ru.itmo.server.core;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itmo.common.commands.base.CommandContext;
import ru.itmo.common.commands.base.CommandShallow;
import ru.itmo.common.commands.base.Response;
import ru.itmo.common.managers.CollectionManager;
import ru.itmo.common.managers.CommandManager;
import ru.itmo.server.managers.DatabaseConnector;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutorService;

public class Handler implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(Handler.class); // Логгер для записи событий и ошибок

    @Setter
    private static CollectionManager collectionManager; // Менеджер коллекции, используется для управления коллекцией объектов
    @Setter
    private static DatabaseConnector databaseConnector; // Соединитель с базой данных, используется для проверки аутентификации и других операций с БД
    @Setter
    private static CommandManager commandManager; // Менеджер команд, используется для выполнения команд

    private CommandShallow shallow; // Объект, представляющий команду, переданную клиентом
    private SelectionKey key; // Ключ выборки для канала, через который пришел запрос
    private SocketAddress client; // Адрес клиента, отправившего запрос
    private ExecutorService senderPool; // Пул потоков для отправки ответов клиентам

    // Конструктор, принимающий команду, ключ выборки, адрес клиента и пул потоков
    public Handler(CommandShallow shallow, SelectionKey key, SocketAddress client, ExecutorService senderPool) {
        this.shallow = shallow;
        this.key = key;
        this.client = client;
        this.senderPool = senderPool;
    }

    // Метод run() будет выполнен в отдельном потоке
    @Override
    public void run() {
        try {
            // Создание контекста команды, который содержит все необходимые данные для выполнения команды
            CommandContext commandContext = new CommandContext(shallow.getArguments(),
                    shallow.getStudyGroup(),
                    collectionManager,
                    shallow.getLogin(),
                    shallow.getPassword());

            // Проверка, залогинен ли пользователь
            if (commandContext.getLogin() == null || commandContext.getPassword() == null) {
                // Если нет, отправляется ответ с сообщением о необходимости входа в систему
                String[] resp = new String[1];
                resp[0] = "Для работы с системой требуется вход в систему";
                Response[] response = Response.createResponses(resp);
                UDPSender sender = new UDPSender(response, key, client);
                senderPool.submit(sender);
                return;
            } else if (!(shallow.getCommand().equals("login") || shallow.getCommand().equals("register"))
                    && !(databaseConnector.isSignIn(commandContext.getLogin(), commandContext.getPassword()))) {
                // Проверка, правильные ли логин и пароль, если команда не является "login" или "register"
                String[] resp = new String[1];
                resp[0] = "Неправильный логин или пароль";
                Response[] response = Response.createResponses(resp);
                UDPSender sender = new UDPSender(response, key, client);
                senderPool.submit(sender);
                return;
            }

            // Выполнение команды и получение ответа
            Response[] response = commandManager.getCommand(shallow.getCommand()).execute(commandContext);
            // Создание и отправка ответа клиенту
            UDPSender sender = new UDPSender(response, key, client);
            senderPool.submit(sender);
        } catch (Exception e) {
            // Логирование ошибки в случае возникновения исключения
            logger.error("Error processing UDP data: " + e.getMessage(), e);
        }
    }
}
