package com.qiyuesuo.util;

import org.apache.commons.codec.binary.Base64;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

/**
 * 数字签名工具类，客户端私钥签名
 */
public class SignUtil {
    public static List<String> sign(){
        List<String> list = new ArrayList<>();
        //keytool生成数据证书时设置的密码
        String password = "adgjmptw258";
        //产生十位数的随机字符串
        String random = getRandomString(10);
        list.add(random);
        String result = null;
        try{
            FileInputStream in = new FileInputStream("E:\\Key\\qiyuesuo.keystore");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(in, password.toCharArray());
            String keyAlias = "";
            Enumeration aliasEnum = keyStore.aliases();
            while (aliasEnum.hasMoreElements()){
                keyAlias = (String)aliasEnum.nextElement();
                System.out.println("别名"+keyAlias);
            }
            //加载私钥
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, password.toCharArray());
            String strKey = Base64.encodeBase64String(privateKey.getEncoded());
            System.out.println("私钥 ：" + strKey);
            //签名，并将结果转码
            result = Base64.encodeBase64String(sign(random.getBytes(),privateKey,"SHA1withRSA"));
            list.add(result);
        }catch (IOException e){
            e.printStackTrace();
        }catch (KeyStoreException e2){
            e2.printStackTrace();
        }catch (CertificateException e3){
            e3.printStackTrace();
        }catch (NoSuchAlgorithmException e4){
            e4.printStackTrace();
        }catch (UnrecoverableKeyException e5){
            e5.printStackTrace();
        }catch (Exception e6){
            e6.printStackTrace();
        }
        return list;
    }
    public static String getRandomString(int length){
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(3);
            long result=0;
            switch(number){
                case 0:
                    result=Math.round(Math.random()*25+65);
                    sb.append(String.valueOf((char)result));
                    break;
                case 1:
                    result=Math.round(Math.random()*25+97);
                    sb.append(String.valueOf((char)result));
                    break;
                case 2:
                    sb.append(String.valueOf(new Random().nextInt(10)));
                    break;
            }

        }
        return sb.toString();
    }


    /**
     *
     * @param message  数据
     * @param privateKey 私钥
     * @param algorithm 签名算法
     * @return
     * @throws Exception
     */
    public static byte[] sign(byte[] message, PrivateKey privateKey, String algorithm) throws Exception {
        Signature signature = Signature.getInstance(algorithm);
        signature.initSign(privateKey);
        signature.update(message);
        return signature.sign();
    }
}
