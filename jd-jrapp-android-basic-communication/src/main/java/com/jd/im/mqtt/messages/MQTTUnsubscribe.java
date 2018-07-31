package com.jd.im.mqtt.messages;


import com.jd.im.mqtt.MQTTException;
import com.jd.im.mqtt.MQTTHelper;
import com.jd.im.storage.Persistentable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import static com.jd.im.mqtt.MQTTConstants.UNSUBSCRIBE;


public class MQTTUnsubscribe extends MQTTMessage implements Persistentable{

    private String[] topicFilters;
    private boolean persistent;

    public MQTTUnsubscribe(byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        topicFilters = new String[10];
        int readed = 0;
        int count = 0;
        try {
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
            setRemainingLength(remainingLength);
            variableHeader = new byte[2];
            System.arraycopy(data, i, variableHeader, 0, variableHeader.length);
            packageIdentifier = new BigInteger(1, variableHeader).intValue();
            dis.read(new byte[i+1]);
            readed = 1;
            while (readed < remainingLength) {
                String topic = decodeString(dis);
                topicFilters[count] = topic;
                readed += (topic.getBytes().length+3);
                count++;
            }
            String[] topics = new String[count];
            System.arraycopy(topicFilters,0,topics,0,count);
            topicFilters = topics;
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String decodeString(DataInputStream in) throws UnsupportedEncodingException {
        try {
            if (in.readByte() < 2) {
                return null;
            }
            int strLen = in.readUnsignedShort();
            byte[] strRaw = new byte[strLen];
            in.read(strRaw);
            return new String(strRaw, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private MQTTUnsubscribe(int identifier, String... topicFilters) {
        this.setType(UNSUBSCRIBE);
        this.topicFilters = topicFilters;

        setPackageIdentifier(identifier);
    }

    public static MQTTUnsubscribe newInstance(int identifier, String... topicFilters) {
        return new MQTTUnsubscribe(identifier, topicFilters);
    }

    public static MQTTUnsubscribe fromBuffer(byte[] content) {
        return new MQTTUnsubscribe(content);
    }

    @Override
    protected byte[] generateFixedHeader() throws MQTTException, IOException {
        // FIXED HEADER
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Type and reserved bits, MUST be [0 0 1 0]
        byte fixed = (byte) ((type << 4) | (0x00 << 3) | (0x00 << 2) | (0x01 << 1) | (0x00 << 0));
        out.write(fixed);

        // Flags (none for UNSUBSCRIBE)

        // Remaining length
        int length = getVariableHeader().length + getPayload().length;
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

        out.write(MQTTHelper.MSB(getPackageIdentifier()));
        out.write(MQTTHelper.LSB(getPackageIdentifier()));

        return out.toByteArray();
    }

    @Override
    protected byte[] generatePayload() throws MQTTException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        if (topicFilters.length <= 0)
            throw new MQTTException("The SUBSCRIBE message must contain at least one topic filter");

        for (int i = 0; i < topicFilters.length; i++) {
            out.write(MQTTHelper.MSB(topicFilters[i].length()));
            out.write(MQTTHelper.LSB(topicFilters[i].length()));
            out.write(topicFilters[i].getBytes());
        }

        return out.toByteArray();
    }

    /**
     * @return The topicFilters
     */
    public String[] getTopicFilters() {
        return topicFilters;
    }

    @Override
    public boolean isPersistent() {
        return persistent;
    }

    @Override
    public void setPersistent() {
        this.persistent = true;
    }
}
