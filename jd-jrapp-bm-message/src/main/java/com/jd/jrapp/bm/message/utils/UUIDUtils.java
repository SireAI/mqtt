package com.sire.corelibrary.Utils;


import java.util.UUID;

/**
 * ==================================================
 * All Right Reserved
 * Date:2017/8/29
 * Author:Sire
 * Description:
 * ==================================================
 */
public class UUIDUtils {
    public static String generateSingleId(){
      return   UUID.randomUUID().toString().replace("-","");
    }
}
