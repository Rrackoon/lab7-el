package ru.itmo;

import org.junit.Test;
import ru.itmo.common.models.*;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class SerializationTest {

    @Test
    public void testStudyGroupSerialization() throws IOException, ClassNotFoundException {
        StudyGroup group = StudyGroup.builder()
                .id(1)
                .name("Test Group")
                .coordinates(new Coordinates(10, 20L))
                .studentsCount(30)
                .expelledStudents(5)
                .shouldBeExpelled(2)
                .formOfEducation(FormOfEducation.FULL_TIME_EDUCATION)
                .groupAdmin(new Person("Admin", "ID12345", Color.BLACK, new Location(1, 2, "Location")))
                .login("login")
                .build();

        // Сериализация
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(group);
        objectOutputStream.flush();
        byte[] bytes = byteArrayOutputStream.toByteArray();
        objectOutputStream.close();

        // Десериализация
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        StudyGroup deserializedGroup = (StudyGroup) objectInputStream.readObject();
        objectInputStream.close();

        // Проверка
        assertEquals(group, deserializedGroup);
    }

    @Test
    public void testPersonSerialization() throws IOException, ClassNotFoundException {
        Person person = new Person("Admin", "ID12345", Color.BLACK, new Location(1, 2, "Location"));

        // Сериализация
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(person);
        objectOutputStream.flush();
        byte[] bytes = byteArrayOutputStream.toByteArray();
        objectOutputStream.close();

        // Десериализация
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Person deserializedPerson = (Person) objectInputStream.readObject();
        objectInputStream.close();

        // Проверка
        assertEquals(person, deserializedPerson);
    }
}
