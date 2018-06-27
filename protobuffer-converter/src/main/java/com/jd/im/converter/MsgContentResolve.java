package com.jd.jrapp.bm.message.model;

import com.google.protobuf.nano.MessageNano;
import com.jd.jrapp.bm.message.db.Message;

import java.util.HashMap;
import java.util.Map;

public class MsgContentResolve {
    private static Map<String,Class>  RELATION = new HashMap<>();
    static {
        RELATION.put("text/plain",String.class);
        RELATION.put("text/plain",String.class);
    }
}
