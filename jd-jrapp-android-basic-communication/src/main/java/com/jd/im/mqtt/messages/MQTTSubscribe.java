package com.jd.im.mqtt.messages;


import com.jd.im.mqtt.MQTTException;
import com.jd.im.mqtt.MQTTHelper;
import com.jd.im.storage.Persistentable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static com.jd.im.mqtt.MQTTConstants.SUBSCRIBE;


public class MQTTSubscribe extends MQTTMessage implements Persistentable{

    private String[] topicFilters;
    private byte[] QoSs;
    private boolean persistent;

    public MQTTSubscribe(byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        topicFilters = new String[10];
        QoSs = new byte[10];
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
            packageIdentifier = (variableHeader[variableHeader.length - 2] >> 8 & 0xFF) | (variableHeader[variableHeader.length - 1] & 0xFF);
            dis.read(new byte[i+1]);
            readed = 1;
            while (readed < remainingLength) {
                String topic = decodeString(dis);
                topicFilters[count] = topic;
                readed += (topic.getBytes().length+3);
                byte qos = (byte) (dis.readByte() & 0x03);
                readed += 1;
                QoSs[count] =  qos;
                count++;
            }
            String[] topics = new String[count];
            System.arraycopy(topicFilters,0,topics,0,count);
            topicFilters = topics;
            byte[] qoss = new byte[count];
            System.arraycopy(QoSs,0,qoss,0,count);
            QoSs = qoss;
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MQTTSubscribe(String[] topicFilters, byte[] QoSs, int identifier) {
        this.setType(SUBSCRIBE);
        this.topicFilters = topicFilters;
        this.QoSs = QoSs;

        setPackageIdentifier(identifier);
    }

    public static MQTTSubscribe newInstance(String[] topicFilters, byte[] QoSs, int identifier) {
        return new MQTTSubscribe(topicFilters, QoSs, identifier);
    }

    public static MQTTSubscribe fromBuffer(byte[] data) {
        return new MQTTSubscribe(data);
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



    @Override
    protected byte[] generateFixedHeader() throws MQTTException, IOException {
        // FIXED HEADER
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Type and reserved bits, MUST be [0 0 1 0]
        byte fixed = (byte) ((type << 4) | (0x00 << 3) | (0x00 << 2) | (0x01 << 1) | (0x00 << 0));
        out.write(fixed);

        // Flags (none for SUBSCRIBE)

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

        if (topicFilters.length <= 0 || QoSs.length <= 0)
            throw new MQTTException("The SUBSCRIBE message must contain at least one topic filter and QoS pair");

        if (topicFilters.length != QoSs.length)
            throw new MQTTException("The SUBSCRIBE message should have the same number of topic filters and QoS");

        for (int i = 0; i < topicFilters.length; i++) {
            out.write(MQTTHelper.MSB(topicFilters[i].length()));
            out.write(MQTTHelper.LSB(topicFilters[i].length()));
            out.write(topicFilters[i].getBytes());
            out.write(QoSs[i]);
        }

        return out.toByteArray();
    }

    /**
     * @return The topicFilters
     */
    public String[] getTopicFilters() {
        return topicFilters;
    }

    /**
     * @return The qoSs
     */
    public byte[] getQoSs() {
        return QoSs;
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
