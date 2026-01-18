package utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AesCryptoUtil {

    // Chave secreta de 32 bytes para AES-256
    // Em produção, isso deveria vir de variável de ambiente ou arquivo de configuração
    private static final String CHAVE_SECRETA = "SoloAndCo2026ChatSecretKey123456"; // 32 caracteres
    private static final String ALGORITMO = "AES";

    /**
     * Criptografa um texto usando AES-256
     * @param textoPlano Texto a ser criptografado
     * @return Texto criptografado em Base64
     * @throws Exception Se houver erro na criptografia
     */
    public static String criptografar(String textoPlano) throws Exception {
        if (textoPlano == null || textoPlano.trim().isEmpty()) {
            throw new IllegalArgumentException("Texto para criptografar não pode ser vazio");
        }

        SecretKeySpec chave = new SecretKeySpec(CHAVE_SECRETA.getBytes("UTF-8"), ALGORITMO);
        Cipher cipher = Cipher.getInstance(ALGORITMO);
        cipher.init(Cipher.ENCRYPT_MODE, chave);
        
        byte[] textoEncriptado = cipher.doFinal(textoPlano.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(textoEncriptado);
    }

    /**
     * Descriptografa um texto usando AES-256
     * @param textoCriptografado Texto criptografado em Base64
     * @return Texto descriptografado
     * @throws Exception Se houver erro na descriptografia
     */
    public static String descriptografar(String textoCriptografado) throws Exception {
        if (textoCriptografado == null || textoCriptografado.trim().isEmpty()) {
            throw new IllegalArgumentException("Texto criptografado não pode ser vazio");
        }

        SecretKeySpec chave = new SecretKeySpec(CHAVE_SECRETA.getBytes("UTF-8"), ALGORITMO);
        Cipher cipher = Cipher.getInstance(ALGORITMO);
        cipher.init(Cipher.DECRYPT_MODE, chave);
        
        byte[] textoDecodificado = Base64.getDecoder().decode(textoCriptografado);
        byte[] textoDescriptografado = cipher.doFinal(textoDecodificado);
        return new String(textoDescriptografado, "UTF-8");
    }
}

