package com.jd.im.mqtt;


public class MQTTConnectionConstants {

  /**
   * We're using this base number to make it less likely that any of these
   * constans will overlap with constants defined in the activity or by MQTT.
   */
  private static final int CONSTANTS_BASE = 846751925;

  /**
   * Doing nothing...
   */
  public static final int STATE_NONE = CONSTANTS_BASE + 0;

  /**
   * Trying to establish connection...
   */
  public static final int STATE_CONNECTING = CONSTANTS_BASE + 1;

  /**
   * Connection established!
   */
  public static final int STATE_CONNECTED = CONSTANTS_BASE + 2;

  /**
   * Connection attempt failed
   */
  public static final int STATE_CONNECTION_FAILED = CONSTANTS_BASE + 3;

  /**
   * Indicate that the state of the connection has changed
   */
  public static final int STATE_CHANGE = CONSTANTS_BASE + 5;

  /**
   * Sent directly when the client has published, can be used if client wants to
   * receive all messages it sends without subscribing.
   */
  public static final int MQTT_RAW_PUBLISH = CONSTANTS_BASE + 10;

  /**
   * Raw byte-array when a message has been read (any message on the stream)
   */
  public static final int MQTT_RAW_READ = CONSTANTS_BASE + 11;

  public static final int CLIENT_MAX_INTERVAL = 8*60000;
  public static final byte MESSAGE_ACK =-7;



  /**
   * Helper to resolve a connection state name.
   *
   * @param state The connection state to translate
   * @return The connection state name
   */
  public static String resolveStateName(int state) {
    switch (state) {
      case STATE_NONE:
        return "STATE_NONE";
      case STATE_CONNECTING:
        return "STATE_CONNECTING";
      case STATE_CONNECTED:
        return "STATE_CONNECTED";
      case STATE_CONNECTION_FAILED:
        return "STATE_CONNECTION_FAILED";

      default:
        return "UNDEFINED STATE";
    }
  }
}
