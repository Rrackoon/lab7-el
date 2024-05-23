package ru.itmo.client.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itmo.common.commands.base.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class UDPSender {
    private static final Logger logger = LoggerFactory.getLogger(UDPSender.class);
    private static final int DATA_SIZE = 1024; // размер каждого чанка

    private final DatagramSocket datagramSocket;
    private final SocketAddress hostAddress;
    private int port;

    public UDPSender(DatagramSocket datagramSocket, SocketAddress hostAddress, int port) {
        this.datagramSocket = datagramSocket;
        this.hostAddress = hostAddress;
        this.port = port;
    }

    public void send(Response[] responses) throws IOException {
        for (Response response : responses) {
            try {
                System.out.println("Передача части ответа");
                logger.debug("response: {}", response.getResponseCount());

                // Сериализуем объект Response в массив байтов
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(response);
                byte[] arr = baos.toByteArray();

                // Разделяем данные на чанки фиксированного размера
                int totalChunks = (int) Math.ceil(arr.length / (double) DATA_SIZE);
                for (int j = 0; j < totalChunks; j++) {
                    int start = j * DATA_SIZE;
                    int length = Math.min(arr.length - start, DATA_SIZE);
                    byte[] chunk = new byte[length + 1];
                    System.arraycopy(arr, start, chunk, 0, length);
                    chunk[length] = (byte) (j == totalChunks - 1 ? 1 : 0); // 1 если последний чанк, иначе 0

                    DatagramPacket datagramPacket = new DatagramPacket(chunk, chunk.length, hostAddress);
                    datagramSocket.send(datagramPacket);
                    logger.info("Отправлено {} байт", chunk.length);
                }
            } catch (IOException e) {
                logger.error("Error sending UDP data: {}", e.getMessage(), e);
            }
        }
    }

    public void send(byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length, hostAddress);
        datagramSocket.send(packet);
    }
}

