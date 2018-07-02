package com.jd.im.mqtt.messages;


import com.jd.im.mqtt.MQTTException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.jd.im.mqtt.MQTTConstants.DISCONNECT;


public class MQTTDisconnect extends MQTTMessage {

    public MQTTDisconnect(byte[] data) {
        int count = 0;
        int i = 0;
        // Type (just for clarity sake we'll set it...)
        this.setType((byte) ((data[i++] >> 4) & 0x0F));
        int multiplier = 1;
        int remainingLength = 0;
        byte digit = 0;
        do {
            digit = data[i++];
            remainingLength += (digit & 127) * multiplier;
            multiplier *= 128;
        } while ((digit & 128) != 0);
        if (remainingLength == -1) {
            return;
        }
        payload = new byte[remainingLength];
        System.arraycopy(data, i, payload, 0, payload.length);
    }


    private MQTTDisconnect() {
        this.setType(DISCONNECT);
    }

    public static MQTTDisconnect newInstance() {
        return new MQTTDisconnect();
    }

    public static MQTTDisconnect fromBuffer(byte[] data) {
        return new MQTTDisconnect(data);
    }

    @Override
    protected byte[] generateFixedHeader() throws MQTTException, IOException {
        // FIXED HEADER
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Type
        out.write((byte) (type << 4));

        // Flags (none for PING)

        // Remaining length
        int length = variableHeader.length + payload.length;
        this.setRemainingLength(length);
        do {
            byte digit = (byte) (length % 128);
            length /= 128;
            if (length > 0)
                digit = (byte) (digit | 0x80);
            out.write(digit);
        } while (length > 0);

        return out.toByteArray();
    }

    @Override
    protected byte[] generateVariableHeader() throws MQTTException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        return out.toByteArray();
    }

    @Override
    protected byte[] generatePayload() throws MQTTException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        return out.toByteArray();
    }
}
