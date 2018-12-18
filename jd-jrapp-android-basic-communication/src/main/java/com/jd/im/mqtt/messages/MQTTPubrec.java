package com.jd.im.mqtt.messages;



import com.jd.im.mqtt.MQTTConstants;
import com.jd.im.mqtt.MQTTException;
import com.jd.im.mqtt.MQTTHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;


public class MQTTPubrec extends MQTTMessage {

  public static MQTTPubrec newInstance(int packageIdentifier) {
    return new MQTTPubrec(packageIdentifier);
  }

  public static MQTTPubrec fromBuffer(byte[] buffer) {
    return new MQTTPubrec(buffer);
  }


  private MQTTPubrec(int packageIdentifier) {
    setType(MQTTConstants.PUBREC);
    setPackageIdentifier(packageIdentifier);
  }

  private MQTTPubrec(byte[] buffer) {

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

    // Get variable header always 2 (just the pkg id) for PUBREC
    variableHeader = new byte[2];

    // We have to step back two bytes since
    System.arraycopy(buffer, i, variableHeader, 0, variableHeader.length);

    // Get payload
    payload = new byte[remainingLength - variableHeader.length];

    if (payload.length > 0)
      System.arraycopy(buffer, i, payload, 0, payload.length);

    // Only get package identifier if the QoS is above AT_MOST_ONCE
    packageIdentifier = (variableHeader[variableHeader.length - 1]) | (variableHeader[variableHeader.length - 2]);
  }

  @Override
  protected byte[] generateFixedHeader() throws MQTTException, IOException {
    // FIXED HEADER
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    // Type and PUBREC flags, reserved bits MUST be [0,0,0,0]
    byte fixed = (byte) ((type << 4) | (0x00 << 3) | (0x00 << 2) | (0x00 << 1) | (0x00) << 0);
    out.write(fixed);

    // Flags (none for PUBREC)

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

    out.write(MQTTHelper.MSB(packageIdentifier));
    out.write(MQTTHelper.LSB(packageIdentifier));

    return out.toByteArray();
  }

  @Override
  protected byte[] generatePayload() throws MQTTException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    return out.toByteArray();
  }

}
