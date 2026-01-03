package utils;

import org.mindrot.jbcrypt.BCrypt;

public class BcryptUtil {

    // Fator de custo (número de rounds). Quanto maior, mais seguro, mas mais lento.
    // 12 é um bom equilíbrio entre segurança e performance
    private static final int ROUNDS = 12;

    /**
     * Criptografa uma senha em texto plano usando BCrypt.
     * 
     * @param senhaTextoPlano A senha em texto plano
     * @return A senha criptografada (hash)
     * @throws IllegalArgumentException se a senha for nula ou vazia
     */
    public static String criptografarSenha(String senhaTextoPlano) {
        if (senhaTextoPlano == null || senhaTextoPlano.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser nula ou vazia");
        }
        
        return BCrypt.hashpw(senhaTextoPlano, BCrypt.gensalt(ROUNDS));
    }

    /**
     * Verifica se uma senha em texto plano corresponde a um hash BCrypt.
     * 
     * @param senhaTextoPlano A senha em texto plano a ser verificada
     * @param senhaCriptografada O hash BCrypt armazenado no banco
     * @return true se a senha corresponder ao hash, false caso contrário
     */
    public static boolean verificarSenha(String senhaTextoPlano, String senhaCriptografada) {
        if (senhaTextoPlano == null || senhaCriptografada == null) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(senhaTextoPlano, senhaCriptografada);
        } catch (IllegalArgumentException e) {
            // Hash inválido ou corrompido
            return false;
        }
    }

    /**
     * Verifica se um hash é um hash BCrypt válido.
     * Útil para migração de senhas antigas.
     * 
     * @param hash O hash a ser verificado
     * @return true se for um hash BCrypt válido, false caso contrário
     */
    public static boolean isBcryptHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            return false;
        }
        
        // Hash BCrypt sempre começa com $2a$, $2b$ ou $2y$
        return hash.matches("^\\$2[ayb]\\$.{56}$");
    }
}


