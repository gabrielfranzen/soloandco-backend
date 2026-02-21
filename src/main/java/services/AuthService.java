package services;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import model.Usuario;
import model.TokenRecuperacaoSenha;
import model.dto.LoginRequest;
import model.dto.RedefinirSenhaRequest;
import model.dto.RefreshRequest;
import model.dto.SolicitarRecuperacaoRequest;
import repository.AuthRepository;
import repository.ResendEmailRepository;
import repository.TokenRecuperacaoSenhaRepository;
import repository.UsuarioRepository;
import utils.BcryptUtil;
import utils.email.TemplateEmailRecuperacaoDeSenha;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthService {

    @EJB
    private AuthRepository authRepository;

    @EJB
    private UsuarioRepository usuarioRepository;

    @EJB
    private TokenRecuperacaoSenhaRepository tokenRecuperacaoRepository;

    @Inject
    private ResendEmailRepository emailService;

    @POST
    @Path("/login")
    public Response login(LoginRequest loginRequest) {
        // O ExceptionMapper vai tratar automaticamente o NotAuthorizedException
        return Response.ok().entity(authRepository.login(loginRequest.getEmail(), loginRequest.getSenha())).build();
    }

    @POST
    @Path("/refresh")
    public Response refresh(RefreshRequest refreshRequest) {
        try {
            return Response.ok().entity(authRepository.refresh(refreshRequest)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/me")
    public Response me(@Context SecurityContext sc, @Context jakarta.ws.rs.container.ResourceInfo info) {
        String email = sc.getUserPrincipal() != null ? sc.getUserPrincipal().getName() : null;
        return Response.ok().entity("{\"email\":\"" + email + "\"}").build();
    }

    // Endpoint para solicitar recuperação de senha
    @POST
    @Path("/solicitar-recuperacao")
    public Response solicitarRecuperacao(@Valid SolicitarRecuperacaoRequest request) {
        try {
            // Busca o usuário pelo e-mail
            var usuarioOpt = usuarioRepository.buscarPorEmail(request.getEmail());
            
            if (usuarioOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"E-mail não encontrado\"}")
                    .build();
            }

            Usuario usuario = usuarioOpt.get();

            // Invalida tokens anteriores do usuário
            tokenRecuperacaoRepository.invalidarTokensAntigos(usuario.getId());

            // Gera token único
            String token = UUID.randomUUID().toString();

            // Calcula data de expiração (30 minutos)
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.MINUTE, 30);
            Date dataExpiracao = cal.getTime();

            // Salva token no banco
            tokenRecuperacaoRepository.criarToken(usuario.getId(), token, dataExpiracao);

            // Monta link de recuperação usando a URL enviada pelo frontend
            String linkRecuperacao = request.getFrontendUrl() + "/recuperar-senha?token=" + token;

            // Gera HTML do e-mail
            String htmlEmail = TemplateEmailRecuperacaoDeSenha.gerarEmailRecuperacaoSenha(usuario.getNome(), linkRecuperacao);

            // Envia e-mail
            emailService.enviarEmail(
                usuario.getEmail(),
                "Recuperação de Senha - Solo & Co",
                htmlEmail,
                true
            );

            return Response.ok()
                .entity("{\"message\":\"Link de recuperação enviado para o seu e-mail\"}")
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Erro ao processar solicitação: " + e.getMessage() + "\"}")
                .build();
        }
    }

    // Endpoint para validar token de recuperação
    @GET
    @Path("/validar-token-recuperacao")
    public Response validarTokenRecuperacao(@QueryParam("token") String token) {
        try {
            if (token == null || token.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Token é obrigatório\"}")
                    .build();
            }

            TokenRecuperacaoSenha tokenRecuperacao = tokenRecuperacaoRepository.buscarPorToken(token);

            if (tokenRecuperacao == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Token inválido\"}")
                    .build();
            }

            if (tokenRecuperacao.getUsado()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Token já utilizado\"}")
                    .build();
            }

            if (!tokenRecuperacao.isValido()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Token expirado\"}")
                    .build();
            }

            return Response.ok()
                .entity("{\"message\":\"Token válido\"}")
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Erro ao validar token: " + e.getMessage() + "\"}")
                .build();
        }
    }

    // Endpoint para redefinir senha
    @POST
    @Path("/redefinir-senha")
    public Response redefinirSenha(@Valid RedefinirSenhaRequest request) {
        try {
            // Valida token
            TokenRecuperacaoSenha tokenRecuperacao = tokenRecuperacaoRepository.buscarPorToken(request.getToken());

            if (tokenRecuperacao == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Token inválido\"}")
                    .build();
            }

            if (tokenRecuperacao.getUsado()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Token já utilizado\"}")
                    .build();
            }

            if (!tokenRecuperacao.isValido()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Token expirado\"}")
                    .build();
            }

            // Busca usuário
            var usuarioOpt = usuarioRepository.buscarPorId(tokenRecuperacao.getUsuarioId());
            
            if (usuarioOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Usuário não encontrado\"}")
                    .build();
            }

            Usuario usuario = usuarioOpt.get();

            // Criptografa nova senha
            String senhaCriptografada = BcryptUtil.criptografarSenha(request.getNovaSenha());
            usuario.setSenha(senhaCriptografada);

            // Atualiza senha no banco
            usuarioRepository.salvar(usuario);

            // Marca token como usado
            tokenRecuperacaoRepository.marcarComoUsado(request.getToken());

            return Response.ok()
                .entity("{\"message\":\"Senha redefinida com sucesso\"}")
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Erro ao redefinir senha: " + e.getMessage() + "\"}")
                .build();
        }
    }
}
