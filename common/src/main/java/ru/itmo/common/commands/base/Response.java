package ru.itmo.common.commands.base;

import ru.itmo.common.utility.ByteActions;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class Response implements Serializable {
    private String message;
    private int responseCount;
    private int responseNumber;

    public Response(String message, int responseCount, int responseNumber) {
        this.message = message;
        this.responseCount = responseCount;
        this.responseNumber = responseNumber;
    }

    public static Response[] createResponses(String[] messages) {
        int maxLength = 394; // Максимальная длина одного сообщения в байтах
        ByteBuffer combinedBuffer = ByteActions.joinStrings(messages);
        ByteBuffer[] splitBuffers = ByteActions.split(combinedBuffer, maxLength);

        Response[] responseArray = new Response[splitBuffers.length];
        for (int i = 0; i < splitBuffers.length; i++) {
            String messagePart = new String(splitBuffers[i].array()).trim();
            responseArray[i] = new Response(messagePart, splitBuffers.length, i + 1);
        }

        return responseArray;
    }

    public String getMessage() {
        return message;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(int responseCount) {
        this.responseCount = responseCount;
    }

    public int getResponseNumber() {
        return responseNumber;
    }

    public void setResponseNumber(int responseNumber) {
        this.responseNumber = responseNumber;
    }
}
