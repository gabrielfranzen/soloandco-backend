package services;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.Usuario;
import repository.UsuarioRepository;
import utils.BcryptUtil;

@Path("/usuario")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UsuarioService {
	
	@Inject
	private UsuarioRepository usuarioRepository;

	@GET
	public Response listar() {
		List<Usuario> usuarios = usuarioRepository.pesquisarTodos();
		usuarios.forEach(this::esconderSenha);
		return Response.ok().entity(usuarios).build();
	}

	@GET
	@Path("/{id}")
	public Response consultar(@PathParam("id") Integer id) {
		Usuario usuario = this.usuarioRepository.consultar(id);
		if (usuario == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("Usuário não encontrado").build();
		}
		esconderSenha(usuario);
		return Response.ok().entity(usuario).build();
	}

	@POST
	public Response cadastrar(Usuario usuario) {
		try {
			if (usuario == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Dados do usuário são obrigatórios").build();
			}

            if (usuario.getNome() == null || usuario.getNome().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Nome é obrigatório").build();
            }

            if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Email é obrigatório").build();
            }

            if (usuario.getTelefone() == null || usuario.getTelefone().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Telefone é obrigatório").build();
            }

			if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Senha é obrigatória").build();
			}

            usuario.setNome(usuario.getNome().trim());
            usuario.setEmail(usuario.getEmail().trim().toLowerCase());
            usuario.setTelefone(usuario.getTelefone().trim());

			String senhaCriptografada = BcryptUtil.criptografarSenha(usuario.getSenha());
			usuario.setSenha(senhaCriptografada);

			if (usuario.getRoles() == null || usuario.getRoles().isBlank()) {
				usuario.setRoles("USER");
			}

			this.usuarioRepository.validarDadosUnicos(usuario);

			Usuario usuarioCadastrado = this.usuarioRepository.inserir(usuario);
			esconderSenha(usuarioCadastrado);
			return Response.ok().entity(usuarioCadastrado).build();

		} catch (Exception e) {
			if (e instanceof IllegalArgumentException) {
				return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Erro interno do servidor: " + e.getMessage())
					.build();
		}
	}

	@PUT
	public Response atualizar(Usuario usuario) {
		try {
			if (usuario == null || usuario.getId() == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity("ID do usuário é obrigatório").build();
			}

			Usuario usuarioExistente = this.usuarioRepository.consultar(usuario.getId());
			if (usuarioExistente == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Usuário não encontrado").build();
			}

			if (usuario.getNome() != null) {
				usuarioExistente.setNome(usuario.getNome());
			}
			if (usuario.getEmail() != null) {
				usuarioExistente.setEmail(usuario.getEmail());
			}
			if (usuario.getTelefone() != null) {
				usuarioExistente.setTelefone(usuario.getTelefone());
			}
			if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
				String senhaCriptografada = BcryptUtil.criptografarSenha(usuario.getSenha());
				usuarioExistente.setSenha(senhaCriptografada);
			}

			this.usuarioRepository.validarDadosUnicos(usuarioExistente, usuarioExistente.getId());

			this.usuarioRepository.atualizar(usuarioExistente);
			
			Usuario atualizado = this.usuarioRepository.consultar(usuarioExistente.getId());
			esconderSenha(atualizado);
			return Response.ok().entity(atualizado).build();

		} catch (Exception e) {
			if (e instanceof IllegalArgumentException) {
				return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Erro interno do servidor: " + e.getMessage())
					.build();
		}
	}

	@DELETE
	@Path("/{id}")
	public Response remover(@PathParam("id") Integer id) {
		try {
			this.usuarioRepository.remover(id);
			return Response.ok().entity("Usuário removido com sucesso").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Erro ao remover usuário: " + e.getMessage())
					.build();
		}
	}

	@POST
	@Path("/email")
	public Response consultarUsuarioPorEmail(String email) {
		var usuarioDTO = this.usuarioRepository.buscarPorEmailDTO(email);
		if (usuarioDTO.isPresent()) {
			return Response.ok().entity(usuarioDTO.get()).build();
		} else {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("Usuário não encontrado")
					.build();
		}
	}

	private Usuario esconderSenha(Usuario usuario) {
		if (usuario != null) {
			usuario.setSenha(null);
		}
		return usuario;
	}

}
