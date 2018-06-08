// IMQTTMessage.aidl
package com.jd.im;

// Declare any non-default types here with import statements

interface IMQTTMessage {

        int getPackageIdentifier();


        byte[] getFixedHeader();


        byte[] getVariableHeader();


        byte[] getPayload();


        byte getType();

        void setType(byte type);

        int getRemainingLength();

        byte[] get();



}
