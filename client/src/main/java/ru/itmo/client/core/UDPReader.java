package ru.itmo.client.core;

import ru.itmo.common.commands.base.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPReader {

    private final DatagramSocket datagramSocket;

    public UDPReader(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public Response readResponse() throws IOException, ClassNotFoundException {
        System.out.println("in readResponse");
        Response resp = null;
        byte[] buffer = new byte[65507];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        datagramSocket.setSoTimeout(3000);
        datagramSocket.receive(packet);
        byte[] data = packet.getData();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        System.out.println("Читаем ответ");
        try {
            resp = (Response) ois.readObject();
            System.out.println("Почучена " + (resp.getResponseCount() + 1) + " из " + resp.getResponseNumber() + " частeй");

        } catch (Exception e) {
            System.out.println("Ответ не Response");

        }
        return resp;
    }
}
