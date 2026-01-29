package services;

import java.net.URLConnection;
import java.util.Base64;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.Usuario;
import model.dto.FotoUsuarioRequest;
import repository.UsuarioRepository;
import repository.utilitarios.ArmazenamentoRepository;
import utils.BcryptUtil;

@Path("/usuario")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UsuarioService {

	private static final int TAMANHO_MAXIMO_FOTO_BYTES = 5 * 1024 * 1024;
	
	@Inject
	private UsuarioRepository usuarioRepository;

	@Inject
	private ArmazenamentoRepository armazenamentoRepository;

	@GET
	public Response listar() {
		List<Usuario> usuarios = usuarioRepository.pesquisarTodos();
		usuarios.forEach(this::prepararUsuarioResposta);
		return Response.ok().entity(usuarios).build();
	}

	@GET
	@Path("/{id}")
	public Response consultar(@PathParam("id") Integer id) {
		Usuario usuario = this.usuarioRepository.consultar(id);
		if (usuario == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("Usuário não encontrado").build();
		}
		prepararUsuarioResposta(usuario);
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
			prepararUsuarioResposta(usuarioCadastrado);
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
			prepararUsuarioResposta(atualizado);
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
	public Response consultarUsuarioPorEmail(java.util.Map<String, String> dados) {
		if (dados == null || !dados.containsKey("email") || dados.get("email") == null || dados.get("email").isBlank()) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Email é obrigatório").build();
		}
		String email = dados.get("email").trim().toLowerCase();
		var usuarioDTO = this.usuarioRepository.buscarPorEmailDTO(email);
		if (usuarioDTO.isPresent()) {
			return Response.ok().entity(usuarioDTO.get()).build();
		} else {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("Usuário não encontrado")
					.build();
		}
	}

	@POST
	@Path("/{id}/foto")
	public Response atualizarFoto(@PathParam("id") Integer id, FotoUsuarioRequest dados) {
		try {
			if (id == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity("ID do usuário é obrigatório").build();
			}
			if (dados == null || dados.getArquivoBase64() == null || dados.getArquivoBase64().isBlank()) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Arquivo é obrigatório").build();
			}

			Usuario usuario = this.usuarioRepository.consultar(id);
			if (usuario == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Usuário não encontrado").build();
			}

			String nomeArquivo = dados.getNomeArquivo();
			if (nomeArquivo == null || nomeArquivo.isBlank()) {
				nomeArquivo = "foto-usuario";
			}

			byte[] arquivo;
			try {
				arquivo = Base64.getDecoder().decode(dados.getArquivoBase64());
			} catch (IllegalArgumentException e) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Arquivo inválido").build();
			}

			if (arquivo.length == 0) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Arquivo é obrigatório").build();
			}
			if (arquivo.length > TAMANHO_MAXIMO_FOTO_BYTES) {
				return Response.status(Response.Status.BAD_REQUEST)
					.entity("Arquivo excede o tamanho máximo de 5MB")
					.build();
			}

			String contentType = dados.getContentType();
			if (contentType == null || contentType.isBlank()) {
				contentType = URLConnection.guessContentTypeFromName(nomeArquivo);
			}
			if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Tipo de arquivo inválido").build();
			}

			String uuidFoto = armazenamentoRepository.upload(nomeArquivo, arquivo, contentType);
			usuario.setUuidFoto(uuidFoto);
			this.usuarioRepository.atualizar(usuario);

			Usuario atualizado = this.usuarioRepository.consultar(id);
			prepararUsuarioResposta(atualizado);
			return Response.ok().entity(atualizado).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity("Erro ao atualizar foto do usuário: " + e.getMessage())
				.build();
		}
	}

	@GET
	@Path("/{id}/foto")
	@Produces("image/*")
	public Response obterFoto(@PathParam("id") Integer id) {
		try {
			if (id == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity("ID do usuário é obrigatório").build();
			}

			Usuario usuario = this.usuarioRepository.consultar(id);
			if (usuario == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Usuário não encontrado").build();
			}
			if (usuario.getUuidFoto() == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Foto do usuário não encontrada").build();
			}

			byte[] arquivo = armazenamentoRepository.download(usuario.getUuidFoto());
			if (arquivo == null || arquivo.length == 0) {
				return Response.status(Response.Status.NOT_FOUND).entity("Foto do usuário não encontrada").build();
			}

			Response.ResponseBuilder response = Response.ok(arquivo);
			String contentType = armazenamentoRepository.obterContentType(usuario.getUuidFoto());
			if (contentType != null && !contentType.isBlank()) {
				response.type(contentType);
			}
			return response.build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity("Erro ao buscar foto do usuário: " + e.getMessage())
				.build();
		}
	}

	private Usuario prepararUsuarioResposta(Usuario usuario) {
		if (usuario != null) {
			usuario.setSenha(null);
		}
		return usuario;
	}

}
