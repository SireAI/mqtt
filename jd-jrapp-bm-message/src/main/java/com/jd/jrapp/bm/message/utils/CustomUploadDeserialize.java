package com.jd.jrapp.bm.message.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.jd.jrapp.bm.message.manager.UploadState;

import java.io.IOException;

public class CustomUploadDeserialize extends JsonDeserializer<UploadState> {


    @Override
    public UploadState deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        return JSONUtils.jsonString2Bean(p.getText(),UploadState.class);
    }
}