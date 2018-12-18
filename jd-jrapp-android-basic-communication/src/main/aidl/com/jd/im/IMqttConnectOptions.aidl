// IMqttConnectOptions.aidl
package com.jd.im;

// Declare any non-default types here with import statements

interface IMqttConnectOptions {
      char[] getPassword();

      String getUserName();

      int getKeepAliveInterval();

      int getConnectionTimeout();

      boolean isCleanSession();

      String[] getServerURIs();

      boolean isAutomaticReconnect();

      String getClientId();


      String  getWillTopic();

      String  getWillMessage();

      boolean  isWillRetain();
      boolean  willFlag();

      int  getWillQoS();

      String getProtocalName();

      int[] getPorts();

      int getReconnectCount();

      int getMessageSendTimeout();


}
