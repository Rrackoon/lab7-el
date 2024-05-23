import ru.itmo.common.commands.base.CommandShallow;
import ru.itmo.common.utility.SGParser;
import ru.itmo.common.utility.StandardPrinter;

import java.io.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Создаем объект CommandShallow
        CommandShallow originalCommand = new CommandShallow("add", "arg1", "user", "pass");
        originalCommand.setStudyGroup(new SGParser(new Scanner(System.in), new StandardPrinter()).parseStudyGroup()
        );
        // Сериализуем объект
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] commandBytes = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(originalCommand);
            out.flush();
            commandBytes = bos.toByteArray();
        } catch (IOException e) {
            System.err.println("Ошибка сериализации: " + e.getMessage());
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                System.err.println("Ошибка закрытия потока: " + ex.getMessage());
            }
        }

        // Десериализуем объект
        ByteArrayInputStream bis = new ByteArrayInputStream(commandBytes);
        ObjectInput in = null;
        CommandShallow deserializedCommand = null;
        try {
            in = new ObjectInputStream(bis);
            deserializedCommand = (CommandShallow) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка десериализации: " + e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                System.err.println("Ошибка закрытия потока: " + ex.getMessage());
            }
        }

        // Выводим результаты
        System.out.println("Оригинальный объект: " + originalCommand.getCommand() + ", " + originalCommand.getArguments());
        System.out.println("Десериализованный объект: " + deserializedCommand.getCommand() + ", " + deserializedCommand.getArguments());
    }
}
