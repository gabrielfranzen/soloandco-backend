package repository;

import java.time.Duration;
import java.util.UUID;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import model.Usuario;
import model.dto.RefreshRequest;
import model.dto.TokenResponse;
import repository.base.AbstractCrudRepository;
import utils.JwtUtil;

@Stateless
public class AuthRepository extends AbstractCrudRepository<Usuario>{

    @EJB
    private UsuarioRepository usuarioRepository;

    @Inject
    private UsuarioPapelRepository usuarioPapelRepository;

    private static final Duration ACCESS_TTL  = Duration.ofMinutes(15);
    private static final Duration REFRESH_TTL = Duration.ofDays(15);

    public TokenResponse login(String email, String senha) {
        // Primeiro verifica se o usuário existe
        var user = usuarioRepository.buscarPorEmail(email);
        
        if (user.isEmpty()) {
            throw new NotAuthorizedException("Email ou senha incorretos");
        }

        // Depois verifica se a senha está correta usando o usuário já buscado
        if (!usuarioRepository.verificarSenha(senha, user.get().getSenha())) {
            throw new NotAuthorizedException("Email ou senha incorretos");
        }

        Usuario usuario = user.get();
        String papeis = usuarioPapelRepository.obterPapeisComoString(usuario.getId());
        
        // Incluir papéis no campo roles do JWT (junto com o legado se existir)
        String rolesParaJwt = usuario.getRoles();
        if (papeis != null && !papeis.isBlank()) {
            if (rolesParaJwt == null || rolesParaJwt.isBlank()) {
                rolesParaJwt = papeis;
            } else {
                // Combinar roles legado com novos papéis
                rolesParaJwt = rolesParaJwt + "," + papeis;
            }
        }

        String access  = JwtUtil.generateToken(usuario.getEmail(), rolesParaJwt, ACCESS_TTL.toMinutes());
        String refresh = UUID.randomUUID().toString();
        usuarioRepository.emitirRefreshToken(usuario, refresh);

        return new TokenResponse(access, refresh, ACCESS_TTL.toSeconds());
    }

    public TokenResponse refresh(RefreshRequest refreshRequest) {

        var user = usuarioRepository.buscarPorRefreshToken(refreshRequest.getRefreshToken())
        .orElseThrow(() -> new NotAuthorizedException("invalid_refresh"));

        if (!usuarioRepository.refreshValido(user, refreshRequest.getRefreshToken())) {
            throw new NotAuthorizedException("invalid_refresh");
        }

        String papeis = usuarioPapelRepository.obterPapeisComoString(user.getId());
        
        // Incluir papéis no campo roles do JWT (junto com o legado se existir)
        String rolesParaJwt = user.getRoles();
        if (papeis != null && !papeis.isBlank()) {
            if (rolesParaJwt == null || rolesParaJwt.isBlank()) {
                rolesParaJwt = papeis;
            } else {
                // Combinar roles legado com novos papéis
                rolesParaJwt = rolesParaJwt + "," + papeis;
            }
        }

        String access  = JwtUtil.generateToken(user.getEmail(), rolesParaJwt, ACCESS_TTL.toMinutes());
        String refresh = UUID.randomUUID().toString(); // rotação de refresh
        usuarioRepository.emitirRefresh(user, refresh, REFRESH_TTL);

        return new TokenResponse(access, refresh, ACCESS_TTL.toSeconds());
    }




}
