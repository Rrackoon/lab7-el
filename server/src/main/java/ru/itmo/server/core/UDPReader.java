package ru.itmo.server.core;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itmo.common.commands.base.CommandShallow;
import ru.itmo.common.managers.CollectionManager;
import ru.itmo.common.managers.CommandManager;
import ru.itmo.common.models.StudyGroup;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutorService;

public class UDPReader extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(UDPReader.class);

    // Статическая строка для хранения входящих данных
    public static String in_string;

    @Setter
    // Коллекция для управления StudyGroup
    public static CollectionManager<StudyGroup> collection;

    @Setter
    // Менеджер команд
    public static CommandManager commandManager;

    // Буфер для чтения данных из UDP-пакетов
    private ByteBuffer in_buffer;

    // Канал для получения данных
    private DatagramChannel channel;

    // Адрес клиента
    private SocketAddress client;

    // Объект команды, который будет десериализован из входящего пакета
    private CommandShallow shallow;

    // Ключ выбора для управления каналом
    private SelectionKey key;

    // Пулы потоков для обработки и отправки данных
    private ExecutorService handlerPool;
    private ExecutorService senderPool;

    // Конструктор для инициализации необходимых компонентов
    public UDPReader(SelectionKey key, ExecutorService handlerPool, ExecutorService senderPool) throws Exception {
        this.key = key;
        in_string = "";
        in_buffer = ByteBuffer.allocateDirect(1024);
        this.channel = (DatagramChannel) key.channel();
        this.handlerPool = handlerPool;
        this.senderPool = senderPool;
    }

    // Получение последней полученной команды
    public CommandShallow getShallow() {
        return shallow;
    }

    // Получение адреса клиента
    public SocketAddress getClient() {
        return this.client;
    }

    @Override
    public void run() {
        try {
            // Получение данных от клиента
            client = receive();
        } catch (Exception e) {
            logger.error("Error during receive: {}", e.getMessage(), e);
        } finally {
            // Установка интересующих операций для ключа и пробуждение селектора
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            key.selector().wakeup();
        }
    }

    // Метод для получения данных от клиента
    public SocketAddress receive() {
        try {
            // Выделение буфера для получения данных
            in_buffer = ByteBuffer.allocate(65507);
            client = channel.receive(in_buffer);

            // Копирование данных из буфера в массив байтов
            byte[] data = new byte[in_buffer.position()];
            System.arraycopy(in_buffer.array(), 0, data, 0, in_buffer.position());

            // Создание потока для чтения объектов из массива байтов
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInput in = new ObjectInputStream(bis);

            // Установка интересующих операций для ключа и пробуждение селектора
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            key.selector().wakeup();

            // Десериализация команды из входящих данных
            shallow = (CommandShallow) in.readObject();
            System.out.println("Получена команда:" + shallow.getCommand());
            logger.info("Command received: " + shallow.getCommand());

            // Очистка буфера после обработки данных
            in_buffer.clear();

            // Создание обработчика для выполнения команды и отправка его в пул потоков
            Handler handler = new Handler(
                    shallow,
                    key,
                    client,
                    senderPool
            );
            handlerPool.submit(handler);
        } catch (Exception e) {
            logger.error("Error receiving UDP data: " + e.getMessage(), e);
        }
        return client;
    }
}
