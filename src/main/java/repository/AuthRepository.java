package repository;

import java.time.Duration;
import java.util.UUID;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
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

        String access  = JwtUtil.generateToken(user.get().getEmail(), user.get().getRoles(), ACCESS_TTL.toMinutes());
        String refresh = UUID.randomUUID().toString();
        usuarioRepository.emitirRefreshToken(user.get(), refresh);

        return new TokenResponse(access, refresh, ACCESS_TTL.toSeconds());
    }

    public TokenResponse refresh(RefreshRequest refreshRequest) {

        var user = usuarioRepository.buscarPorRefreshToken(refreshRequest.getRefreshToken())
        .orElseThrow(() -> new NotAuthorizedException("invalid_refresh"));

        if (!usuarioRepository.refreshValido(user, refreshRequest.getRefreshToken())) {
            throw new NotAuthorizedException("invalid_refresh");
        }

        String access  = JwtUtil.generateToken(user.getEmail(), user.getRoles(), ACCESS_TTL.toMinutes());
        String refresh = UUID.randomUUID().toString(); // rotação de refresh
        usuarioRepository.emitirRefresh(user, refresh, REFRESH_TTL);

        return new TokenResponse(access, refresh, ACCESS_TTL.toSeconds());
    }




}
