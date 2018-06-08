package com.jd.im.mqtt.messages;



import com.jd.im.mqtt.MQTTException;

import java.io.IOException;



public class MQTTPingresp extends MQTTMessage {

  public static MQTTPingresp fromBuffer(byte[] buffer) {
    return new MQTTPingresp(buffer);
  }


  private MQTTPingresp(byte[] buffer) {

    int i = 0;
    // Type (just for clarity sake we'll read it...)
    this.setType((byte) ((buffer[i++] >> 4) & 0x0F));

    // Remaining length
    this.setRemainingLength(0);

    // No variable header
    setVariableHeader(null);

    // No payload
    setPayload(null);
  }

  @Override
  protected byte[] generateFixedHeader() throws MQTTException, IOException {
    // Client doesn't create the PINGRESP
    return null;
  }

  @Override
  protected byte[] generateVariableHeader() throws MQTTException, IOException {
    // Client doesn't create the PINGRESP
    return null;
  }

  @Override
  protected byte[] generatePayload() throws MQTTException, IOException {
    // Client doesn't create the PINGRESP
    return null;
  }

}
