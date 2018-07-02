package com.jd.jrapp.bm.message.constant;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/21
 * Author:wangkai
 * Description:
 * =====================================================
 */
public interface Constant {
    /**
     * 实时
     */
    String TOPIC_REAL_TIME = "RT";
    /**
     * 离线
     */
    String TOPIC_OFF_LINE = "SF";
    /**
     * SF兼容RT，相比较RT，能收到离线消息
     */
    String TOPIC_SF = "SF?types:1,2,3,4,5,6,7,8,9,11,12,13,14";

    int T_SENDING = 1;
    int T_FAILED = 2;
    int T_SUCCESS = 3;
    String PEER_TALKER = "peerTalker";
    String CONNECT_NAME = "connect_name";
    String CONNECT_PASSWORD = "connect_password";
    String TALKER = "talker";
    String ID_PRIFIX = "pin:";
    String NET_ERROR = "netError";
    String FILE_SEND_FAILED = "fileSendFailed";
    String IMAGE_UPLOAD_SUCCESS = "0";

    int FILE_MESSAGE_IMAGE = 4;
}
