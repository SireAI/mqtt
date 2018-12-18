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
  public static final int STATE_NONE = 0;

    /**
     * Connection attempt failed
     */
    public static final int STATE_CONNECTION_FAILED =  1;

  /**
   * Trying to establish connection...
   */
  public static final int STATE_CONNECTING = 2;

  /**
   * Connection established!
   */
  public static final int STATE_CONNECTED = 3;


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
