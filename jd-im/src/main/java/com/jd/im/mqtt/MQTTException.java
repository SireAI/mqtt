package com.jd.im.mqtt;


public class MQTTException extends Exception {

  public MQTTException() {
    super("MQTT Exception");
  }

  public MQTTException(String message) {
    super(message);
  }

  public MQTTException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
