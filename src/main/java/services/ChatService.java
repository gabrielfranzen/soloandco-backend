package services;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import model.*;
import model.dto.*;
import repository.*;
import utils.ChatLongPollingManager;

@Path("/chat")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ChatService {

    @Inject
    private ChatSalaRepository salaRepository;

    @Inject
    private ChatMensagemRepository mensagemRepository;

    @Inject
    private ChatParticipanteRepository participanteRepository;

    @Inject
    private UsuarioRepository usuarioRepository;

    @Inject
    private EstabelecimentoRepository estabelecimentoRepository;

    /**
     * POST /chat/salas/{estabelecimentoId}/entrar
     * Entra em uma sala de chat do estabelecimento (verifica check-in válido)
     */
    @POST
    @Path("/salas/{estabelecimentoId}/entrar")
    public Response entrarNaSala(@PathParam("estabelecimentoId") Integer estabelecimentoId,
                                  @Context SecurityContext sc) {
        try {
            // Valida autenticação
            Optional<Usuario> usuarioOpt = obterUsuarioAutenticado(sc);
            if (usuarioOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não autenticado\"}")
                        .build();
            }

            Usuario usuario = usuarioOpt.get();

            // Valida estabelecimento
            Optional<Estabelecimento> estabelecimentoOpt = estabelecimentoRepository.buscarAtivoPorId(estabelecimentoId);
            if (estabelecimentoOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Estabelecimento não encontrado\"}")
                        .build();
            }

            Estabelecimento estabelecimento = estabelecimentoOpt.get();

            // Busca ou cria sala
            ChatSala sala = salaRepository.buscarOuCriar(estabelecimento);

            // Verifica se já tem acesso válido
            if (participanteRepository.verificarAcessoValido(usuario.getId(), sala.getId())) {
                // Já tem acesso, retorna informações da sala
                ChatSalaResponse response = montarSalaResponse(sala, usuario.getId());
                return Response.ok(response).build();
            }

            // Não tem acesso válido, precisa ter feito check-in recente
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Você precisa fazer check-in no estabelecimento para acessar o chat\"}")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Erro ao entrar na sala: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /chat/salas/{salaId}/mensagens
     * Lista mensagens de uma sala com paginação (requer acesso válido)
     * @param before ID da mensagem antes da qual buscar (scroll up - mensagens antigas)
     * @param after ID da mensagem após a qual buscar (sincronização - mensagens novas)
     * @param limit Número máximo de mensagens (padrão 20, máximo 100)
     */
    @GET
    @Path("/salas/{salaId}/mensagens")
    public Response listarMensagens(@PathParam("salaId") Integer salaId,
                                     @QueryParam("before") Integer before,
                                     @QueryParam("after") Integer after,
                                     @QueryParam("limit") Integer limit,
                                     @Context SecurityContext sc) {
        try {
            // Valida autenticação
            Optional<Usuario> usuarioOpt = obterUsuarioAutenticado(sc);
            if (usuarioOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não autenticado\"}")
                        .build();
            }

            Usuario usuario = usuarioOpt.get();

            // Valida acesso à sala
            if (!participanteRepository.verificarAcessoValido(usuario.getId(), salaId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\":\"Você não tem acesso a esta sala ou seu acesso expirou\"}")
                        .build();
            }

            // Define limite (padrão 20, máximo 100)
            int limiteConsulta = (limit != null && limit > 0 && limit <= 100) ? limit : 20;
            
            List<ChatMensagem> mensagens;
            
            // Determina o tipo de consulta baseado nos parâmetros
            if (before != null) {
                // Scroll up: buscar mensagens anteriores (mais antigas)
                mensagens = mensagemRepository.listarMensagensAnteriores(salaId, before, limiteConsulta);
                // Reverter ordem para cronológica (mais antiga primeiro)
                java.util.Collections.reverse(mensagens);
            } else if (after != null) {
                // Sincronização: buscar mensagens novas após um ponto
                mensagens = mensagemRepository.listarMensagensPosteriores(salaId, after);
            } else {
                // Carregamento inicial: últimas N mensagens
                mensagens = mensagemRepository.listarUltimasMensagens(salaId, limiteConsulta);
            }

            // Converte para DTO descriptografando
            List<ChatMensagemResponse> response = mensagens.stream()
                    .map(this::converterParaMensagemResponse)
                    .collect(Collectors.toList());

            return Response.ok(response).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Erro ao listar mensagens: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /chat/salas/{salaId}/mensagens/poll
     * Long Polling: aguarda até 30 segundos por novas mensagens após um ID específico
     * @param after ID da última mensagem recebida pelo cliente (obrigatório)
     */
    @GET
    @Path("/salas/{salaId}/mensagens/poll")
    public Response longPollMensagens(@PathParam("salaId") Integer salaId,
                                       @QueryParam("after") Integer after,
                                       @Context SecurityContext sc) {
        try {
            // Valida parâmetro obrigatório
            if (after == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Parâmetro 'after' é obrigatório para Long Polling\"}")
                        .build();
            }

            // Valida autenticação
            Optional<Usuario> usuarioOpt = obterUsuarioAutenticado(sc);
            if (usuarioOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não autenticado\"}")
                        .build();
            }

            Usuario usuario = usuarioOpt.get();

            // Valida acesso à sala
            if (!participanteRepository.verificarAcessoValido(usuario.getId(), salaId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\":\"Você não tem acesso a esta sala ou seu acesso expirou\"}")
                        .build();
            }

            // Verifica imediatamente se já há novas mensagens
            List<ChatMensagem> mensagensNovas = mensagemRepository.listarMensagensPosteriores(salaId, after);
            
            if (!mensagensNovas.isEmpty()) {
                // Já tem mensagens novas, retorna imediatamente
                List<ChatMensagemResponse> response = mensagensNovas.stream()
                        .map(this::converterParaMensagemResponse)
                        .collect(Collectors.toList());
                return Response.ok(response).build();
            }

            // Não tem mensagens novas ainda, aguarda até 30 segundos
            boolean notificado = ChatLongPollingManager.aguardarComTimeout(salaId, 30, TimeUnit.SECONDS);
            
            if (notificado) {
                // Foi notificado, busca as novas mensagens
                mensagensNovas = mensagemRepository.listarMensagensPosteriores(salaId, after);
                List<ChatMensagemResponse> response = mensagensNovas.stream()
                        .map(this::converterParaMensagemResponse)
                        .collect(Collectors.toList());
                return Response.ok(response).build();
            } else {
                // Timeout: retorna array vazio
                return Response.ok(List.of()).build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Erro no Long Polling: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * POST /chat/salas/{salaId}/mensagens
     * Envia uma mensagem em uma sala (requer acesso válido)
     */
    @POST
    @Path("/salas/{salaId}/mensagens")
    public Response enviarMensagem(@PathParam("salaId") Integer salaId,
                                    ChatMensagemRequest request,
                                    @Context SecurityContext sc) {
        try {
            // Valida request
            if (request == null || request.getMensagem() == null || request.getMensagem().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Mensagem é obrigatória\"}")
                        .build();
            }

            if (request.getMensagem().length() > 1000) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Mensagem muito longa (máximo 1000 caracteres)\"}")
                        .build();
            }

            // Valida autenticação
            Optional<Usuario> usuarioOpt = obterUsuarioAutenticado(sc);
            if (usuarioOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não autenticado\"}")
                        .build();
            }

            Usuario usuario = usuarioOpt.get();

            // Valida acesso à sala
            if (!participanteRepository.verificarAcessoValido(usuario.getId(), salaId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\":\"Você não tem acesso a esta sala ou seu acesso expirou\"}")
                        .build();
            }

            // Busca sala
            ChatSala sala = salaRepository.consultar(salaId);
            if (sala == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Sala não encontrada\"}")
                        .build();
            }

            // Insere mensagem (criptografada automaticamente)
            ChatMensagem mensagem = mensagemRepository.inserirMensagem(sala, usuario, request.getMensagem());

            // Notifica threads aguardando por mensagens nesta sala (Long Polling)
            ChatLongPollingManager.notificarNovaMensagem(salaId);

            // Retorna resposta descriptografada
            ChatMensagemResponse response = converterParaMensagemResponse(mensagem);

            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Erro ao enviar mensagem: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /chat/minhas-salas
     * Lista todas as salas que o usuário tem acesso válido
     */
    @GET
    @Path("/minhas-salas")
    public Response listarMinhasSalas(@Context SecurityContext sc) {
        try {
            // Valida autenticação
            Optional<Usuario> usuarioOpt = obterUsuarioAutenticado(sc);
            if (usuarioOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não autenticado\"}")
                        .build();
            }

            Usuario usuario = usuarioOpt.get();

            // Lista participações ativas
            List<ChatParticipante> participacoes = participanteRepository.listarSalasDoUsuario(usuario.getId());

            // Converte para DTO
            List<ChatSalaResponse> response = participacoes.stream()
                    .map(p -> {
                        ChatSalaResponse dto = new ChatSalaResponse();
                        dto.setId(p.getSala().getId());
                        dto.setEstabelecimentoId(p.getSala().getEstabelecimento().getId());
                        dto.setEstabelecimentoNome(p.getSala().getEstabelecimento().getNome());
                        dto.setAtivo(p.getSala().getAtivo());
                        dto.setCriadoEm(p.getSala().getCriadoEm());
                        dto.setAcessoExpiraEm(p.getAcessoExpiraEm());
                        return dto;
                    })
                    .collect(Collectors.toList());

            return Response.ok(response).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Erro ao listar salas: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /chat/minhas-salas-detalhadas
     * Lista todas as salas com informações detalhadas (última mensagem, participantes)
     */
    @GET
    @Path("/minhas-salas-detalhadas")
    public Response listarMinhasSalasDetalhadas(@Context SecurityContext sc) {
        try {
            // Valida autenticação
            Optional<Usuario> usuarioOpt = obterUsuarioAutenticado(sc);
            if (usuarioOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não autenticado\"}")
                        .build();
            }

            Usuario usuario = usuarioOpt.get();

            // Lista participações ativas
            List<ChatParticipante> participacoes = participanteRepository.listarSalasDoUsuario(usuario.getId());

            // Converte para DTO detalhado
            List<ChatSalaDetalhadaResponse> response = participacoes.stream()
                    .map(p -> {
                        ChatSalaDetalhadaResponse dto = new ChatSalaDetalhadaResponse();
                        dto.setSalaId(p.getSala().getId());
                        dto.setEstabelecimentoId(p.getSala().getEstabelecimento().getId());
                        dto.setEstabelecimentoNome(p.getSala().getEstabelecimento().getNome());
                        dto.setAcessoExpiraEm(p.getAcessoExpiraEm());
                        
                        // Busca última mensagem
                        ChatMensagem ultimaMensagem = mensagemRepository.buscarUltimaMensagemDaSala(p.getSala().getId());
                        if (ultimaMensagem != null) {
                            ChatSalaDetalhadaResponse.UltimaMensagemDTO msgDto = new ChatSalaDetalhadaResponse.UltimaMensagemDTO();
                            
                            // Descriptografa a mensagem
                            try {
                                String textoDescriptografado = mensagemRepository.descriptografar(ultimaMensagem.getMensagem());
                                msgDto.setTexto(textoDescriptografado);
                            } catch (Exception e) {
                                msgDto.setTexto("[Erro ao descriptografar]");
                            }
                            
                            msgDto.setUsuarioNome(ultimaMensagem.getUsuario() != null ? ultimaMensagem.getUsuario().getNome() : "Desconhecido");
                            msgDto.setCriadoEm(ultimaMensagem.getCriadoEm());
                            dto.setUltimaMensagem(msgDto);
                        }
                        
                        // Conta participantes ativos
                        Integer totalParticipantes = participanteRepository.contarParticipantesAtivos(p.getSala().getId());
                        dto.setTotalParticipantesAtivos(totalParticipantes);
                        
                        // Por enquanto, mensagens não lidas sempre 0
                        dto.setMensagensNaoLidas(0);
                        
                        return dto;
                    })
                    .collect(Collectors.toList());

            return Response.ok(response).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Erro ao listar salas detalhadas: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET /chat/salas/{salaId}/participantes
     * Lista participantes ativos de uma sala
     */
    @GET
    @Path("/salas/{salaId}/participantes")
    public Response listarParticipantes(@PathParam("salaId") Integer salaId,
                                        @Context SecurityContext sc) {
        try {
            // Valida autenticação
            Optional<Usuario> usuarioOpt = obterUsuarioAutenticado(sc);
            if (usuarioOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não autenticado\"}")
                        .build();
            }

            Usuario usuario = usuarioOpt.get();

            // Valida acesso à sala
            if (!participanteRepository.verificarAcessoValido(usuario.getId(), salaId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\":\"Você não tem acesso a esta sala ou seu acesso expirou\"}")
                        .build();
            }

            // Lista participantes ativos
            List<ChatParticipante> participantes = participanteRepository.listarParticipantesAtivos(salaId);

            // Converte para DTO
            List<ChatParticipanteResponse> response = participantes.stream()
                    .map(this::converterParaParticipanteResponse)
                    .collect(Collectors.toList());

            return Response.ok(response).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Erro ao listar participantes: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // ==================== Métodos auxiliares ====================

    private Optional<Usuario> obterUsuarioAutenticado(SecurityContext sc) {
        if (sc == null || sc.getUserPrincipal() == null) {
            return Optional.empty();
        }
        String email = sc.getUserPrincipal().getName();
        return usuarioRepository.buscarPorEmail(email);
    }

    private ChatMensagemResponse converterParaMensagemResponse(ChatMensagem mensagem) {
        ChatMensagemResponse dto = new ChatMensagemResponse();
        dto.setId(mensagem.getId());
        dto.setSalaId(mensagem.getSala() != null ? mensagem.getSala().getId() : null);
        dto.setUsuarioId(mensagem.getUsuario() != null ? mensagem.getUsuario().getId() : null);
        dto.setUsuarioNome(mensagem.getUsuario() != null ? mensagem.getUsuario().getNome() : null);
        dto.setUsuarioEmail(mensagem.getUsuario() != null ? mensagem.getUsuario().getEmail() : null);
        
        // Descriptografa mensagem
        try {
            String mensagemPlain = mensagemRepository.descriptografar(mensagem.getMensagem());
            dto.setMensagem(mensagemPlain);
        } catch (Exception e) {
            dto.setMensagem("[Erro ao descriptografar mensagem]");
        }
        
        dto.setCriadoEm(mensagem.getCriadoEm());
        dto.setEditadoEm(mensagem.getEditadoEm());
        return dto;
    }

    private ChatParticipanteResponse converterParaParticipanteResponse(ChatParticipante participante) {
        ChatParticipanteResponse dto = new ChatParticipanteResponse();
        dto.setId(participante.getId());
        if (participante.getUsuario() != null) {
            dto.setUsuarioId(participante.getUsuario().getId());
            dto.setUsuarioNome(participante.getUsuario().getNome());
            dto.setUsuarioEmail(participante.getUsuario().getEmail());
        }
        dto.setAcessoExpiraEm(participante.getAcessoExpiraEm());
        dto.setCriadoEm(participante.getCriadoEm());
        return dto;
    }

    private ChatSalaResponse montarSalaResponse(ChatSala sala, Integer usuarioId) {
        ChatSalaResponse dto = new ChatSalaResponse();
        dto.setId(sala.getId());
        dto.setEstabelecimentoId(sala.getEstabelecimento() != null ? sala.getEstabelecimento().getId() : null);
        dto.setEstabelecimentoNome(sala.getEstabelecimento() != null ? sala.getEstabelecimento().getNome() : null);
        dto.setAtivo(sala.getAtivo());
        dto.setCriadoEm(sala.getCriadoEm());
        
        // Busca data de expiração do participante
        Optional<ChatParticipante> participanteOpt = participanteRepository.buscarParticipante(usuarioId, sala.getId());
        participanteOpt.ifPresent(p -> dto.setAcessoExpiraEm(p.getAcessoExpiraEm()));
        
        return dto;
    }
}

