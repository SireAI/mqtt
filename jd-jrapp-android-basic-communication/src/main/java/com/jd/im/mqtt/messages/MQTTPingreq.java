package com.jd.im.mqtt.messages;


import com.jd.im.mqtt.MQTTException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


import static com.jd.im.mqtt.MQTTConstants.PINGREQ;


public class MQTTPingreq extends MQTTMessage {

  public static MQTTPingreq newInstance() {
    return new MQTTPingreq();
  }


  private MQTTPingreq() {
    this.setType(PINGREQ);
  }

  @Override
  protected byte[] generateFixedHeader() throws MQTTException, IOException {
    // FIXED HEADER
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    // Type
    byte fixed = (byte) (type << 4);
    out.write(fixed);

    // Flags (none for PINGREQ)

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
    return out.toByteArray();
  }

  @Override
  protected byte[] generatePayload() throws MQTTException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    return out.toByteArray();
  }

}
