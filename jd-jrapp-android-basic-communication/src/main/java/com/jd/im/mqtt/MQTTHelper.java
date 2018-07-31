package com.jd.im.mqtt;


import android.os.RemoteException;

import com.jd.im.IMQTTMessage;
import com.jd.im.mqtt.messages.MQTTMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import static com.jd.im.mqtt.MQTTConstants.CONNACK;
import static com.jd.im.mqtt.MQTTConstants.CONNECT;
import static com.jd.im.mqtt.MQTTConstants.DISCONNECT;
import static com.jd.im.mqtt.MQTTConstants.PINGREQ;
import static com.jd.im.mqtt.MQTTConstants.PINGRESP;
import static com.jd.im.mqtt.MQTTConstants.PUBACK;
import static com.jd.im.mqtt.MQTTConstants.PUBCOMP;
import static com.jd.im.mqtt.MQTTConstants.PUBLISH;
import static com.jd.im.mqtt.MQTTConstants.PUBREC;
import static com.jd.im.mqtt.MQTTConstants.PUBREL;
import static com.jd.im.mqtt.MQTTConstants.SUBACK;
import static com.jd.im.mqtt.MQTTConstants.SUBSCRIBE;
import static com.jd.im.mqtt.MQTTConstants.UNSUBACK;
import static com.jd.im.mqtt.MQTTConstants.UNSUBSCRIBE;

public class MQTTHelper {

//  private static int lastPackageIdentifier = 1;

  private static final String UTF8 = "UTF-8";

  /**
   * Detects if string is encoded with UTF-8
   *
   * @param buffer String
   * @return true if UTF-8, false otherwise
   */
  public static boolean isUTF8(byte[] buffer) {
//    UniversalDetector detector = new UniversalDetector(null);
////    detector.handleData(buffer, 0, buffer.length);
////    detector.dataEnd();
////
////    String encoding = detector.getDetectedCharset();
////    detector.reset();

    return true;
  }

  public static boolean hasWildcards(String topic) {
    // TODO, is the $ acceptable?
    return /*topic.contains("/") ||*/ topic.contains("#") || topic.contains("+");
  }

//  public static int getNewPackageIdentifier() {
//    return (lastPackageIdentifier += 1);
//  }

  /**
   * Return the MSB of a string length
   *
   * @param buffer String
   * @return MSB
   */
  public static byte MSB(byte[] buffer) {
    return (byte) ((buffer.length) >> 8 & 0xFF);
  }

  /**
   * Return the LSB of the string length
   *
   * @param buffer String
   * @return LSB
   */
  public static byte LSB(byte[] buffer) {
    return (byte) (buffer.length & 0xFF);
  }

  /**
   * Return most significant byte of integer
   *
   * @param val the value
   * @return the MSB
   */
  public static byte MSB(int val) {
    return (byte) ((val & 0xffff) >> 8 & 0xFF);
  }

  /**
   * Return least significant byte of integer
   *
   * @param val the value
   * @return the LSB
   */
  public static byte LSB(int val) {
    return (byte) ((val & 0xffff) & 0xFF);
  }

  public static void decodeFlags(byte flags) {
    boolean username = ((flags >> 7) == 1 ? true : false);

    boolean password = ((flags >> 6) == 1 ? true : false);
  }

  /**
   * Decode message type
   *
   * @param buffer The message buffer to decode
   * @return The message type
   */
  public static byte decodeType(byte[] buffer) {
    return (byte) ((buffer[0] >> 4) & 0x0F);
  }

  /**
   * Get the human readable name of a message type
   *
   * @param message the message
   * @return human readable string of message type
   */
  public static String decodePackageName(IMQTTMessage message) throws RemoteException {
    return decodePackageName(message.getType());
  }

  /**'
   * Get the human readable name of a message type
   *
   * @param messageType the message type
   * @return human readable string of message type
   */
  public static String decodePackageName(byte messageType) {
    switch (messageType) {
    case CONNECT:
      return "CONNECT";
    case CONNACK:
      return "CONNACK";
    case PUBLISH:
      return "PUBLISH";
    case PUBACK:
      return "PUBACK";
    case PUBREC:
      return "PUBREC";
    case PUBREL:
      return "PUBREL";
    case PUBCOMP:
      return "PUBCOMP";
    case SUBSCRIBE:
      return "SUBSCRIBE";
    case SUBACK:
      return "SUBACK";
    case UNSUBSCRIBE:
      return "UNSUBSCRIBE";
    case UNSUBACK:
      return "UNSUBACK";
    case PINGREQ:
      return "PINGREQ";
    case PINGRESP:
      return "PINGRESP";
    case DISCONNECT:
      return "DISCONNECT";
    default:
      return "Unknown message type";
    }
  }
  public static MultiByteInteger readMBI(DataInputStream in) throws IOException {

    byte digit;
    long msgLength = 0;
    int multiplier = 1;
    int count = 0;

    do {
      digit = in.readByte();
      count++;
      msgLength += ((digit & 0x7F) * multiplier);
      multiplier *= 128;
    } while ((digit & 0x80) != 0);

    return new MultiByteInteger(msgLength, count);
  }

  public static byte[] encodeMBI( long number) {
    int numBytes = 0;
    long no = number;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    // Encode the remaining length fields in the four bytes
    do {
      byte digit = (byte)(no % 128);
      no = no / 128;
      if (no > 0) {
        digit |= 0x80;
      }
      bos.write(digit);
      numBytes++;
    } while ( (no > 0) && (numBytes<4) );

    return bos.toByteArray();
  }
}
