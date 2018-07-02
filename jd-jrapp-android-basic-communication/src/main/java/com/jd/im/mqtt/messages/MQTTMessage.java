package com.jd.im.mqtt.messages;


import com.jd.im.IMQTTMessage;
import com.jd.im.mqtt.MQTTException;
import com.jd.im.mqtt.MQTTHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;



public  class MQTTMessage extends IMQTTMessage.Stub {

    protected byte flags;

    protected int packageIdentifier;

    protected byte[] fixedHeader;
    protected byte[] variableHeader;
    protected byte[] payload;

    protected byte type;
    protected int remainingLength;


    protected  byte[] generateFixedHeader() throws MQTTException, IOException{
        return new byte[0];
    }

    protected  byte[] generateVariableHeader() throws MQTTException, IOException{
        return new byte[0];
    }

    protected  byte[] generatePayload() throws MQTTException, IOException{
        return payload;
    }

    /**
     * @return The packageIdentifier
     */
//  @Override
    public int getPackageIdentifier() {
        return packageIdentifier;
    }

    /**
     * @param packageIdentifier The packageIdentifier to set
     */
//  @Override
    public void setPackageIdentifier(int packageIdentifier) {
        this.packageIdentifier = packageIdentifier;
    }

    /**
     * @return The fixedHeader
     */
    @Override
    public byte[] getFixedHeader() {
        return fixedHeader;
    }

    /**
     * @param fixedHeader The fixedHeader to set
     */
    public void setFixedHeader(byte[] fixedHeader) {
        this.fixedHeader = fixedHeader;
    }

    /**
     * @return The variableHeader
     */
    @Override
    public byte[] getVariableHeader() {
        return variableHeader;
    }

    /**
     * @param variableHeader The variableHeader to set
     */
    public void setVariableHeader(byte[] variableHeader) {
        this.variableHeader = variableHeader;
    }

    /**
     * @return The payload
     */
    @Override
    public byte[] getPayload() {
        return payload;
    }

    /**
     * @param payload The payload to set
     */
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    /**
     * @return The type
     */
    @Override
    public byte getType() {
        return type;
    }

    /**
     * @param type The type to set
     */
    public void setType(byte type) {
        this.type = type;
    }

    /**
     * @return The remainingLength
     */
    @Override
    public int getRemainingLength() {
        return remainingLength;
    }

    /**
     * @param remainingLength The remainingLength to set
     */
    public void setRemainingLength(int remainingLength) {
        this.remainingLength = remainingLength;
    }



    public byte[] get() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            this.payload = generatePayload();
            this.variableHeader = generateVariableHeader();
            this.fixedHeader = generateFixedHeader();

            out.write(fixedHeader);
            out.write(variableHeader);
            out.write(payload);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MQTTException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("MQTT Package ").append("\n");
        sb.append("  type: " + MQTTHelper.decodePackageName(this.getType())).append("\n");
        sb.append("  remaining length: " + this.getRemainingLength()).append("\n");
        sb.append("  package identifier: " + this.getPackageIdentifier());

        return sb.toString();
    }

}
