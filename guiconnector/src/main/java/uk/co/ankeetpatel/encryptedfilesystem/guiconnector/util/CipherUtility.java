package uk.co.ankeetpatel.encryptedfilesystem.guiconnector.util;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;


public class CipherUtility {

    private static CipherUtility instance;
    private KeyPairGenerator keyPairGenerator;

    private final SecureRandom random = new SecureRandom();

    public KeyPair getKeyPair() {
        return keyPairGenerator.genKeyPair();
    }

    public byte[] encryptBytes(byte[] content, Key pubKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] cipherContent = cipher.doFinal(content);
        return cipherContent;
    }

    public byte[] decryptBytes(byte[] cipherContent, Key privKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privKey);
        byte[] decryptedContent = cipher.doFinal(cipherContent);
        return decryptedContent;
    }

    public byte[] decryption(ArrayList<byte[]> bytes, Key privKey) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        ArrayList<byte[]> decryptedByteList = new ArrayList<>();
        for (byte[] a : bytes) {
            byte[] decryptarr = decryptBytes(a, privKey);
            decryptedByteList.add(decryptarr);
        }
        byte[] messageArray = decryptedByteList.get(0);
        for (int i = 1; i < decryptedByteList.size(); i++) {
            messageArray = CipherUtility.combine(messageArray, decryptedByteList.get(i));
        }
        return messageArray;
    }

    public ArrayList<byte[]> encryption(byte[] byteFile, Key pubKey) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        ArrayList<byte[]> byteArrays = new ArrayList<>();

        for (int i = 0; i < byteFile.length; i += 117) {
            byte[] arr = Arrays.copyOfRange(byteFile, i, Math.min(byteFile.length,i+117));
            byteArrays.add(arr);
        }
        ArrayList<byte[]> encryptedByteList = new ArrayList<>();
        for (byte[] a : byteArrays) {
            byte[] encryptarr = encryptBytes(a, pubKey);
            encryptedByteList.add(encryptarr);
        }
        return encryptedByteList;
    }

    public static byte[] combine(byte[] a, byte[] b) {
        int length = a.length + b.length;
        byte[] result = new byte[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public String encodeKey(Key key) {
        byte[] keyBytes = key.getEncoded();
        String encodedKeyStr = Base64.getEncoder().encodeToString(keyBytes);
        return encodedKeyStr;
    }

    public PublicKey decodePublicKey(String keyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.getDecoder().decode(keyStr);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey key = keyFactory.generatePublic(spec);
        return key;
    }

    public PrivateKey decodePrivateKey(String keyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.getDecoder().decode(keyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey key = keyFactory.generatePrivate(keySpec);
        return key;
    }

    private CipherUtility() throws NoSuchAlgorithmException {
        keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024, random);
    }

    public static CipherUtility getInstance() throws NoSuchAlgorithmException {
        if (instance == null) {
            instance = new CipherUtility();
        }
        return instance;
    }

}

