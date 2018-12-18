package com.jd.im.mqtt.messages;


import com.jd.im.mqtt.MQTTException;

import java.io.IOException;
import java.math.BigInteger;


public class MQTTSuback extends MQTTMessage {

  public static MQTTSuback fromBuffer(byte[] buffer) {
    return new MQTTSuback(buffer);
  }


  private MQTTSuback(byte[] buffer) {
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

    // Get variable header (always length 2 in SUBACK)
    variableHeader = new byte[2];
    System.arraycopy(buffer, i, variableHeader, 0, variableHeader.length);

    // Get payload
    payload = new byte[remainingLength - variableHeader.length];
    if (payload.length > 0)
      System.arraycopy(buffer, i + variableHeader.length, payload, 0, remainingLength - variableHeader.length);

    // Get package identifier

    packageIdentifier = (variableHeader[variableHeader.length - 2] >> 8 & 0xFF) | (variableHeader[variableHeader.length - 1] & 0xFF);  }

  @Override
  protected byte[] generateFixedHeader() throws MQTTException, IOException {
    // Client doesn't create SUBACK
    return null;
  }

  @Override
  protected byte[] generateVariableHeader() throws MQTTException, IOException {
    // Client doesn't create SUBACK
    return null;
  }

  @Override
  protected byte[] generatePayload() throws MQTTException, IOException {
    // Client doesn't create SUBACK
    return null;
  }

}
