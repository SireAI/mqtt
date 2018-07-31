package com.jd.im.mqtt;

import com.jd.im.mqtt.messages.MQTTPuback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.sql.SQLOutput;
import java.util.Arrays;

import static com.jd.im.mqtt.MQTTConstants.CONNECT;
import static com.jd.im.mqtt.MQTTConstants.DISCONNECT;
import static com.jd.im.mqtt.MQTTConstants.PUBACK;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/7/30
 * Author:wangkai
 * Description: 切分数据首位
 * =====================================================
 */
public class MqttMessageSpliter  {
    private DataInputStream in;
    private ByteArrayOutputStream bais;
    private long remLen;
    private long packetLen;
    private byte[] packet;

    public MqttMessageSpliter(InputStream in) {
        this.in = new DataInputStream(in);
        this.bais = new ByteArrayOutputStream();
        this.remLen = -1;
    }

    public byte[] readSingleMessageBytes() throws IOException {
        // read header
        if (remLen < 0) {
            // Assume we can read the whole header at once.
            // The header is very small so it's likely we
            // are able to read it fully or not at all.
            // This keeps the parser lean since we don't
            // need to cope with a partial header.
            // Should we lose synch with the stream,
            // the keepalive mechanism would kick in
            // closing the connection.
            bais.reset();

            byte first = in.readByte();

            byte type = (byte) ((first >>> 4) & 0x0F);
            if ((type < CONNECT) ||
                    (type > DISCONNECT)) {
                return new byte[0];
            }

            remLen = MQTTHelper.readMBI(in).getValue();

            bais.write(first);
            // bit silly, we decode it then encode it
            bais.write(MQTTHelper.encodeMBI(remLen));
            packet = new byte[(int) (bais.size() + remLen)];
            packetLen = 0;
        }

        // read remaining packet
        if (remLen >= 0) {
            // the remaining packet can be read with timeouts

            byte[] header = bais.toByteArray();
            readFully();
            System.arraycopy(header, 0, packet, 0, header.length);

            // reset packet parsing state
            remLen = -1;

        }

        return packet;
    }


    private void readFully() throws IOException {
        int off = bais.size() + (int) packetLen;
        int len = (int) (remLen - packetLen);
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;

        while (n < len) {
            int count = -1;
            try {
                count = in.read(packet, off + n, len - n);
            } catch (SocketTimeoutException e) {
                // remember the packet read so far
                packetLen += n;
                throw e;
            }
            if (count < 0)
                throw new EOFException();
            n += count;
        }
    }
}
