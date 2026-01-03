package utils;

import java.util.Date;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import model.Usuario;
import utils.BcryptUtil;
import repository.UsuarioRepository;

@Singleton
@Startup
public class Seed {

    @EJB
    private UsuarioRepository usuarioRepository;

    @PostConstruct
    public void init() {
        if (usuarioRepository.buscarPorEmail("admin@exemplo.com").isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setEmail("admin@exemplo.com");
            usuario.setNome("Administrador");
            usuario.setTelefone("11999990000");
            usuario.setSenha(BcryptUtil.criptografarSenha("admin123"));
            usuario.setRoles("ADMIN,USER");
            usuario.setDataCadastro(new Date());
            usuario.setDataAtualizacao(new Date());
            usuarioRepository.inserir(usuario);
        }
    }
}