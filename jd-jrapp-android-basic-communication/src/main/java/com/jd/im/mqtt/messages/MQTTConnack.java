package com.jd.im.mqtt.messages;



import com.jd.im.mqtt.MQTTException;

import java.io.IOException;



public class MQTTConnack extends MQTTMessage {

  @SuppressWarnings("unused")
  private byte RESERVED;
  private byte returnCode;

  public static MQTTConnack fromBuffer(byte[] buffer) {
    return new MQTTConnack(buffer);
  }

  private MQTTConnack(byte[] buffer) {
    int i = 0;

    // Type (just for clarity sake we'll set it...)
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

    // Get length of protocol name
    len = ((buffer[i] >> 8) & 0xFF) | (buffer[i + 1] & 0xFF);

    // Get variable header (always length 2 in CONNACK)
    variableHeader = new byte[2];
    System.arraycopy(buffer, i, variableHeader, 0, variableHeader.length);

    // Get payload
    payload = new byte[remainingLength - variableHeader.length];
    if (payload.length > 0)
      System.arraycopy(buffer, i + variableHeader.length, payload, 0, remainingLength - variableHeader.length);

    RESERVED = variableHeader[0];
    returnCode = variableHeader[1];
  }

  /**
   * @return the returnCode
   */
  public byte getReturnCode() {
    return returnCode;
  }

  @Override
  protected byte[] generateFixedHeader() throws IOException {
    // Client doesn't generate CONNACK
    return null;
  }

  @Override
  protected byte[] generateVariableHeader() throws MQTTException, IOException {
    // Client doesn't generate CONNACK
    return null;
  }

  @Override
  protected byte[] generatePayload() throws MQTTException, IOException {
    // Client doesn't generate CONNACK
    return null;
  }

}
