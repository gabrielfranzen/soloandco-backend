package utils;

import java.util.List;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import model.Usuario;
import repository.UsuarioRepository;

/**
 * Utilitário para migrar senhas em texto plano para BCrypt.
 * 
 * ATENÇÃO: Esta classe roda automaticamente na inicialização do servidor.
 * Ela verifica todos os usuários e atualiza senhas não criptografadas.
 * 
 * Após a migração completa, você pode remover ou comentar esta classe.
 */
@Singleton
@Startup
public class MigrarSenhasBcrypt {

    private static final Logger LOGGER = Logger.getLogger(MigrarSenhasBcrypt.class.getName());

    @EJB
    private UsuarioRepository usuarioRepository;

    @PostConstruct
    public void migrarSenhas() {
        try {
            LOGGER.info("=== INICIANDO MIGRAÇÃO DE SENHAS PARA BCRYPT ===");
            
            // Busca todos os usuários
            List<Usuario> usuarios = usuarioRepository.pesquisarTodos();
            int totalMigrados = 0;
            int totalJaCriptografados = 0;
            
            for (Usuario usuario : usuarios) {
                String senhaAtual = usuario.getSenha();
                
                // Verifica se a senha já está criptografada com BCrypt
                if (senhaAtual != null && !BcryptUtil.isBcryptHash(senhaAtual)) {
                    LOGGER.info("Migrando senha do usuário: " + usuario.getNome());
                    
                    // A senha atual está em texto plano, vamos criptografá-la
                    String senhaCriptografada = BcryptUtil.criptografarSenha(senhaAtual);
                    usuario.setSenha(senhaCriptografada);
                    
                    // Atualiza no banco
                    usuarioRepository.atualizar(usuario);
                    totalMigrados++;
                    
                    LOGGER.info("✓ Senha migrada para o usuário: " + usuario.getNome());
                } else {
                    totalJaCriptografados++;
                }
            }
            
            LOGGER.info("=== MIGRAÇÃO CONCLUÍDA ===");
            LOGGER.info("Total de senhas migradas: " + totalMigrados);
            LOGGER.info("Total já criptografadas: " + totalJaCriptografados);
            LOGGER.info("Total de usuários: " + usuarios.size());
            
            if (totalMigrados > 0) {
                LOGGER.warning("ATENÇÃO: " + totalMigrados + " senha(s) foram migradas para BCrypt.");
                LOGGER.warning("Você pode remover ou comentar a classe MigrarSenhasBcrypt.java após confirmar que tudo funcionou.");
            }
            
        } catch (Exception e) {
            LOGGER.severe("Erro ao migrar senhas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


