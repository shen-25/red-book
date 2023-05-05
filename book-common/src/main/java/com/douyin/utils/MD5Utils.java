package com.douyin.utils;

import org.apache.tomcat.util.codec.binary.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

    public static  final String SALT = "^&*%$dkK·120-__+&^%';(jm6y65rtgr";

    public static String getMD5Str(String strValue) {

        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String res = Base64.encodeBase64String(md5.digest((strValue + SALT).getBytes()));
        return res;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        String md5Str = getMD5Str("test");
        System.out.println(md5Str);
    }
}
