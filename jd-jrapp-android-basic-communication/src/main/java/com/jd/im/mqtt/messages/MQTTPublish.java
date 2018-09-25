package com.jd.im.mqtt.messages;


import android.os.RemoteException;

import com.jd.im.IVariableHeaderExtraPart;
import com.jd.im.mqtt.MQTTException;
import com.jd.im.mqtt.MQTTHelper;
import com.jd.im.storage.Persistentable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import static com.jd.im.mqtt.MQTTConstants.AT_LEAST_ONCE;
import static com.jd.im.mqtt.MQTTConstants.AT_MOST_ONCE;
import static com.jd.im.mqtt.MQTTConstants.EXACTLY_ONCE;
import static com.jd.im.mqtt.MQTTConstants.PUBLISH;


public class MQTTPublish extends MQTTMessage implements Persistentable {

    private boolean dup;
    private byte QoS;
    private boolean retain;

    private String topicName;
    /**
     * 可变头追加控制信息
     */
    private IVariableHeaderExtraPart extraHeaderPart;
    private boolean persistent;

    private MQTTPublish(String topic, byte[] payload, int identifier) {
        this(topic, payload, AT_MOST_ONCE, identifier);
    }

    private MQTTPublish(String topic, byte[] payload, byte QoS, int identifier) {
        this.type = PUBLISH;

        this.topicName = topic;
        this.payload = payload;

        this.QoS = QoS;

        if (this.QoS > AT_MOST_ONCE) {
            setPackageIdentifier(identifier);
        }
    }

    private MQTTPublish(byte[] buffer) {
        int i = 0;

        // Type (just for clarity sake we'll set it...)
        this.retain = (buffer[i] >> 0 & 1) == 0x01 ? true : false;
        this.QoS = (byte) ((buffer[i] & ((0x00 << 3) | (0x01 << 2) | (0x01 << 1) | (0x00 << 0))) >> 1);
        this.dup = ((buffer[i] >> 3) & 1) == 1 ? true : false;
        this.setType((byte) ((buffer[i++] >> 4) & 0x0F));

        // Remaining length
        int multiplier = 1;
        int len = 0;
        byte digit = 0;
        do {
            digit = buffer[i++];
            len += (digit & 127) * multiplier;
            multiplier *= 128;
        } while ((digit & 128) != 0);
        this.setRemainingLength(len);

        len = (byte) ((buffer[i] >> 8 & 0xFF) | (buffer[i + 1] & 0xFF));

        // Get variable header (topic length + 2[topic len] + 2[pkg id])

        switch (getQoS()) {
            case AT_MOST_ONCE:
                // No packageIdentifier
                variableHeader = new byte[len + 2];
                break;

            case AT_LEAST_ONCE:
            case EXACTLY_ONCE:
                // 2 byte packageIdentifier
                variableHeader = new byte[len + 2 + 2];
                break;
            default:
                variableHeader = new byte[len + 2];
        }
        //如果出现此类情况，原因可能是流中的首尾标记切分出现问题
        if ((buffer.length - i) < variableHeader.length) {
            return;
        }
        System.arraycopy(buffer, i, variableHeader, 0, variableHeader.length);
        i += variableHeader.length;

        // Get topic
        topicName = new String(variableHeader, 2, len);

        // Get payload
        payload = new byte[remainingLength - variableHeader.length];

        switch (getQoS()) {
            case AT_MOST_ONCE:
                // No packageIdentifier
                break;

            case AT_LEAST_ONCE:
            case EXACTLY_ONCE:
                // 2 byte packageIdentifier
                packageIdentifier = new BigInteger(1, variableHeader).intValue();
                break;
        }

        System.arraycopy(buffer, i, payload, 0, payload.length);
    }

    public static MQTTPublish fromBuffer(byte[] buffer) {
        return new MQTTPublish(buffer);
    }

    public static MQTTPublish newInstance(String topic, byte[] payload, int identifier) {
        return new MQTTPublish(topic, payload, identifier);
    }

    public static MQTTPublish newInstance(String topic, byte[] payload, byte QoS, int identifier) {
        return new MQTTPublish(topic, payload, QoS, identifier);
    }

    @Override
    protected byte[] generateFixedHeader() throws MQTTException, IOException {
        // FIXED HEADER
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte type = this.type;
        byte dup = (byte) (this.dup ? 0x01 : 0x00);
        byte QoS = this.QoS;
        byte retain = (byte) (this.retain ? 0x01 : 0x00);

        // Type and PUBLISH flags
        byte fixed = (byte) ((type << 4) | (dup << 3) | (QoS << 1) | (retain << 0));
        out.write(fixed);

        // Flags (none for PUBLISH)

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

        // TOPIC
        // Topic MUST be UTF-8

        // Topic MUST NOT contain wildcards
        if (MQTTHelper.hasWildcards(topicName))
            throw new MQTTException("Invalid topic, may not contain wildcards");
        out.write(MQTTHelper.MSB(topicName.length()));
        out.write(MQTTHelper.LSB(topicName.length()));
        out.write(topicName.getBytes("UTF-8"));

        // Package identifier ONLY exists if QoS is above AT_MOST_ONCE
        if (QoS > AT_MOST_ONCE) {
            // Package identifier MUST exist for AT_LEAST_ONCE and EXACTLY_ONCE
            if (packageIdentifier == 0)
                throw new MQTTException("Package identifier must not be 0");

            out.write(MQTTHelper.MSB(packageIdentifier));
            out.write(MQTTHelper.LSB(packageIdentifier));
        }
        //extra
        if (extraHeaderPart != null) {
            try {
                byte[] bytes = extraHeaderPart.extraVariableHeaderPart();
                out.write(bytes);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return out.toByteArray();
    }

    @Override
    protected byte[] generatePayload() throws MQTTException, IOException {
        return payload;
    }

    /**
     * Mark this message as duplicate
     */
    public void setDup() {
        this.dup = true;
    }

    /**
     * @return The dup
     */
    public boolean isDup() {
        return dup;
    }

    /**
     * @return The qoS
     */
    public byte getQoS() {
        return QoS;
    }

    /**
     * @return The retain
     */
    public boolean isRetain() {
        return retain;
    }

    /**
     * @param retain The retain to set
     */
    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    /**
     * @return The topicName
     */
    public String getTopicName() {
        return topicName;
    }

    public void setExtraHeaderPart(IVariableHeaderExtraPart extraHeaderPart) {
        this.extraHeaderPart = extraHeaderPart;
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
