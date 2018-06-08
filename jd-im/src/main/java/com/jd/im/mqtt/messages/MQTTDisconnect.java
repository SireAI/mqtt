package com.jd.im.mqtt.messages;


import com.jd.im.mqtt.MQTTException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


import static com.jd.im.mqtt.MQTTConstants.DISCONNECT;


public class MQTTDisconnect extends MQTTMessage {

  public static MQTTDisconnect newInstance() {
    return new MQTTDisconnect();
  }


  private MQTTDisconnect() {
    this.setType(DISCONNECT);
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
