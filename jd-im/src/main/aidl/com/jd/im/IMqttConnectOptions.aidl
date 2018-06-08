// IMqttConnectOptions.aidl
package com.jd.im;

// Declare any non-default types here with import statements
import com.jd.im.IMQTTMessage;
import com.jd.im.IVariableHeaderExtraPart;

interface IMqttConnectOptions {
      char[] getPassword();

      String getUserName();

      int getKeepAliveInterval();

      int getConnectionTimeout();

      boolean isCleanSession();

      String[] getServerURIs();

      boolean isAutomaticReconnect();

      String getClientId();

      String getWillDestination();

      IMQTTMessage getWillMessage();

      String getProtocalName();

      int[] getPorts();

      int getReconnectCount();

      IVariableHeaderExtraPart getExtraHeaderPart();

}
