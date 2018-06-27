package com.sire.corelibrary.Utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * ==================================================
 * All Right Reserved
 * Date:2017/1/4
 * Author:sire
 * Description:
 * ==================================================
 */
public class JSONUtils {
    private static ObjectMapper mapper;

    private static void checkInstance() {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
    }


    /**
     * format key and value to json string
     *
     * @param jsonMap
     * @return
     */
    public static String String2Json(Map<String, Object> jsonMap)  {

        if (jsonMap == null || jsonMap.size() == 0) {
            return null;
        }
        checkInstance();
        try {
            return mapper.writeValueAsString(jsonMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * format json string to bean
     *
     * @param jsonString
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T jsonString2Bean(String jsonString, Class<T> clazz) {
        checkInstance();
        if (jsonString == null) {
            return null;
        }
        try {

            return mapper.readValue(jsonString,clazz);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static <T> T jsonString2Bean(String jsonString, TypeReference<T> typeReference) {
        checkInstance();
        if (jsonString == null) {
            return null;
        }
        try {

            return mapper.readValue(jsonString,typeReference);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getJsonValue(String jsonString, String key) {
        if (jsonString == null) {
            return "";
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return jsonObject.get(key).toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String bean2JsonString(Object src) {
        checkInstance();
        try {
            return mapper.writeValueAsString(src);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
