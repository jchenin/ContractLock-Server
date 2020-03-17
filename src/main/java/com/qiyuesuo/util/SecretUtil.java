package com.qiyuesuo.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;

/**
 * 加密解密类
 */
public class SecretUtil {
    public static byte[] stbb = null;
    public static void crypt(InputStream in, OutputStream out, Cipher cipher) throws IOException, GeneralSecurityException {
        int blockSize = cipher.getBlockSize();
        int outputSize = cipher.getOutputSize(blockSize);
        byte[] inBytes = new byte[blockSize];
        byte[] outBytes = new byte[outputSize];
        int inLength = 0;
        boolean more = true;
        while (more){
            inLength = in.read(inBytes);
            if(inLength == blockSize){
                int outLength = cipher.update(inBytes, 0, blockSize, outBytes);
                System.out.println("---" + Base64.decodeBase64(outBytes));
                out.write(Base64.decodeBase64(outBytes), 0, outLength);
            }else {
                more = false;
            }
        }
        if(inLength > 0){
            outBytes = cipher.doFinal(inBytes, 0, inLength);
        }else {
            outBytes = cipher.doFinal();
        }
        System.out.println("加密后文件大小字节："  +outBytes.length);
        for(byte b : outBytes){
            System.out.print(b);
        }
        System.out.println("---" + Base64.decodeBase64(outBytes));
        out.write(Base64.decodeBase64(outBytes));
    }
    public static String encrypt(String publicKeyPath, InputStream in, String encryptPath){
        String result = null;
        try{
            //生成密钥
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
//            SecureRandom random = new SecureRandom();
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed("qiyuesuo".getBytes());
            keyGenerator.init(128, random);
            SecretKey key = keyGenerator.generateKey();
            System.out.println("初始aes密钥 " + Base64.encodeBase64String(key.getEncoded()));
            //读取公钥
            ObjectInputStream keyIn = new ObjectInputStream(new FileInputStream(publicKeyPath));
            //读取明文
//            InputStream oin = new FileInputStream(in);
            //加密后文件
            DataOutputStream dout = new DataOutputStream(new FileOutputStream(encryptPath));
            Key publicKey = (Key)keyIn.readObject();
            //非对称加密,加密上面的AES的随机密钥
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.WRAP_MODE, publicKey);
            byte[] wrappedKey = cipher.wrap(key);
            result = Base64.encodeBase64String(wrappedKey);
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            System.out.println("你好长度：" + stbb.length);
            for(int j = 0; j < "你好".getBytes().length; j++){
                System.out.println("你好".getBytes()[j]);
            }
            stbb = cipher.doFinal("你好".getBytes());
            System.out.println("长度：" + stbb.length);
            for(byte rrb : stbb) {
                System.out.println(rrb);
            }
//            System.out.println("你好".getBytes());
//            System.out.println("你好 "  + Base64.encodeBase64(stbb));
//            ByteArrayOutputStream  ba = new ByteArrayOutputStream();
//            ba.write(stbb);
//            ba.close();
//            cipher = Cipher.getInstance("AES");
//            cipher.init(Cipher.DECRYPT_MODE, key);
//            byte[] bbbb = cipher.doFinal(stbb);
//            String sss = Base64.encodeBase64String(bbbb);
            SecretUtil.crypt(in, dout, cipher);
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }catch(IOException e2){
            e2.printStackTrace();
        }catch (ClassNotFoundException e3){
            e3.printStackTrace();
        }catch (NoSuchPaddingException e4){
            e4.printStackTrace();
        }catch (InvalidKeyException e5){
            e5.printStackTrace();
        }catch (IllegalBlockSizeException e6){
            e6.printStackTrace();
        }catch (GeneralSecurityException e7){
            e7.printStackTrace();
        }
        return result;
    }

    /**
     * RSA生成公钥私钥
     * @param publicPath 公钥地址
     * @param privatePath 私钥地址
     */
    public static void genRsaKey(String publicPath, String privatePath){
        try{
            KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = new SecureRandom();
            pairGenerator.initialize(512, random);
            KeyPair keyPair = pairGenerator.generateKeyPair();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(publicPath));
            out.writeObject(keyPair.getPublic());
            ObjectOutputStream out2 = new ObjectOutputStream(new FileOutputStream(privatePath));
            out2.writeObject(keyPair.getPrivate());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 解密
     * @param encryPath 密文地址
     * @param decryPath  解密后的文件地址
     * @param privateKeyPath  RSA产生的私钥地址
     * @param token  数字信封
     * */
    public static void decrypt(String encryPath, String decryPath, String privateKeyPath, String token){
        try {
            File file = new File(decryPath);
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        try(InputStream in = new FileInputStream(encryPath);
            ObjectInputStream keyIn = new ObjectInputStream(new FileInputStream(privateKeyPath));
            OutputStream out = new FileOutputStream(decryPath)){
            byte[] b = new byte[1024];
            int len;
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            while((len = in.read(b)) != -1){
                bao.write(b, 0, len);
            }
            Key privateKey = (Key)keyIn.readObject();
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.UNWRAP_MODE, privateKey);
            Key key = cipher.unwrap(Base64.decodeBase64(token), "AES", Cipher.SECRET_KEY);
            System.out.println("解密后的aes " + Base64.encodeBase64String(key.getEncoded()));
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] entry= Files.readAllBytes(Paths.get(encryPath));
            byte[] decry = cipher.doFinal(Base64.decodeBase64(entry));
            Files.write(Paths.get(decryPath), decry);
//            SecretUtil.crypt(in, out, cipher);
        }catch (IOException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e2){
            e2.printStackTrace();
        }catch (NoSuchAlgorithmException e3){
            e3.printStackTrace();
        }catch (NoSuchPaddingException e4){
            e4.printStackTrace();
        }catch (InvalidKeyException e5){
            e5.printStackTrace();
        }catch (GeneralSecurityException e6){
            e6.printStackTrace();
        }
    }

}
