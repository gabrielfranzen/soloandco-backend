package services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import model.Checkin;
import model.ChatSala;
import model.Estabelecimento;
import model.Papel;
import model.Usuario;
import model.dto.CheckinDetalheDTO;
import model.dto.CheckinRequest;
import model.dto.CheckinResponse;
import model.dto.EstabelecimentoComEstatisticasDTO;
import repository.CheckinRepository;
import repository.ChatSalaRepository;
import repository.ChatParticipanteRepository;
import repository.EstabelecimentoRepository;
import repository.PapelRepository;
import repository.UsuarioPapelRepository;
import repository.UsuarioRepository;

@Path("/estabelecimentos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EstabelecimentoService {

    private static final double RAIO_MAXIMO_METROS = 50.0;

    @Inject
    private EstabelecimentoRepository estabelecimentoRepository;

    @Inject
    private CheckinRepository checkinRepository;

    @Inject
    private UsuarioRepository usuarioRepository;

    @Inject
    private ChatSalaRepository chatSalaRepository;

    @Inject
    private ChatParticipanteRepository chatParticipanteRepository;

    @Inject
    private PapelRepository papelRepository;

    @Inject
    private UsuarioPapelRepository usuarioPapelRepository;

    @GET
    public Response listar() {
        List<Estabelecimento> ativos = estabelecimentoRepository.listarAtivos();
        return Response.ok(ativos).build();
    }

    @POST
    public Response cadastrar(Estabelecimento estabelecimento, @Context SecurityContext sc) {
        try {
            if (estabelecimento == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Dados do estabelecimento são obrigatórios\"}")
                        .build();
            }

            if (estabelecimento.getNome() == null || estabelecimento.getNome().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Nome é obrigatório\"}")
                        .build();
            }

            if (estabelecimento.getLatitude() == null || estabelecimento.getLongitude() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Latitude e longitude são obrigatórias\"}")
                        .build();
            }

            if (estabelecimento.getAtivo() == null) {
                estabelecimento.setAtivo(true);
            }

            // Obter usuário autenticado
            String emailUsuario = sc != null && sc.getUserPrincipal() != null 
                ? sc.getUserPrincipal().getName() 
                : null;
                
            if (emailUsuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não autenticado\"}")
                        .build();
            }

            var usuarioOpt = usuarioRepository.buscarPorEmail(emailUsuario);
            if (usuarioOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não encontrado\"}")
                        .build();
            }

            Usuario proprietario = usuarioOpt.get();
            estabelecimento.setProprietario(proprietario);

            // Atribuir papel de empresário ao proprietário se ainda não tiver
            if (!usuarioPapelRepository.usuarioTemPapel(proprietario.getId(), Papel.CODIGO_EMPRESARIO)) {
                var papelEmpresarioOpt = papelRepository.buscarPorCodigo(Papel.CODIGO_EMPRESARIO);
                if (papelEmpresarioOpt.isPresent()) {
                    usuarioPapelRepository.atribuirPapel(proprietario, papelEmpresarioOpt.get());
                }
            }

            Estabelecimento cadastrado = estabelecimentoRepository.inserir(estabelecimento);
            return Response.status(Response.Status.CREATED).entity(cadastrado).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Erro ao cadastrar estabelecimento: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/{id}/checkin")
    public Response registrarCheckin(@PathParam("id") Integer estabelecimentoId,
                                     CheckinRequest request,
                                     @Context SecurityContext sc) {
        try {
            if (request == null || request.getLatitude() == null || request.getLongitude() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Latitude e longitude são obrigatórias\"}")
                        .build();
            }

            Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorEmail(
                    sc != null && sc.getUserPrincipal() != null ? sc.getUserPrincipal().getName() : null);
            if (usuarioOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não autenticado\"}")
                        .build();
            }

            Optional<Estabelecimento> estabelecimentoOpt = estabelecimentoRepository.buscarAtivoPorId(estabelecimentoId);
            if (estabelecimentoOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Estabelecimento não encontrado\"}")
                        .build();
            }

            Estabelecimento estabelecimento = estabelecimentoOpt.get();
            double distancia = calcularDistanciaMetros(
                    request.getLatitude(), request.getLongitude(),
                    estabelecimento.getLatitude(), estabelecimento.getLongitude());

            if (distancia > RAIO_MAXIMO_METROS) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Você precisa estar a até 50 metros para fazer check-in\"}")
                        .build();
            }

            Checkin checkin = checkinRepository.registrar(usuarioOpt.get(), estabelecimento, distancia);
            
            // Integração com chat: adiciona usuário à sala automaticamente
            try {
                ChatSala sala = chatSalaRepository.buscarOuCriar(estabelecimento);
                chatParticipanteRepository.adicionarOuAtualizarParticipante(sala, usuarioOpt.get(), checkin);
            } catch (Exception chatEx) {
                // Log do erro mas não falha o check-in
                System.err.println("Erro ao adicionar usuário ao chat: " + chatEx.getMessage());
            }
            
            CheckinResponse resposta = montarResposta(checkin);

            return Response.ok(resposta).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Erro ao registrar check-in\"}")
                    .build();
        }
    }

    @GET
    @Path("/{id}/checkins")
    public Response listarCheckins(@PathParam("id") Integer estabelecimentoId) {
        Optional<Estabelecimento> estabelecimentoOpt = estabelecimentoRepository.buscarAtivoPorId(estabelecimentoId);
        if (estabelecimentoOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Estabelecimento não encontrado\"}")
                    .build();
        }

        List<Checkin> lista = checkinRepository.listarPorEstabelecimento(estabelecimentoId);
        List<CheckinDetalheDTO> dto = lista.stream()
                .map(this::converterParaDetalhe)
                .collect(Collectors.toList());

        return Response.ok(dto).build();
    }

    @GET
    @Path("/meus")
    public Response listarMeusEstabelecimentos(@Context SecurityContext sc) {
        try {
            String emailUsuario = sc != null && sc.getUserPrincipal() != null 
                ? sc.getUserPrincipal().getName() 
                : null;
                
            if (emailUsuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não autenticado\"}")
                        .build();
            }

            var usuarioOpt = usuarioRepository.buscarPorEmail(emailUsuario);
            if (usuarioOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não encontrado\"}")
                        .build();
            }

            Usuario usuario = usuarioOpt.get();
            
            // Verificar se o usuário é empresário
            if (!usuarioPapelRepository.usuarioTemPapel(usuario.getId(), Papel.CODIGO_EMPRESARIO)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\":\"Acesso permitido apenas para empresários\"}")
                        .build();
            }

            List<Estabelecimento> estabelecimentos = estabelecimentoRepository.listarPorProprietario(usuario.getId());
            List<EstabelecimentoComEstatisticasDTO> resultado = estabelecimentos.stream()
                    .map(e -> converterParaEstatisticas(e))
                    .collect(Collectors.toList());

            return Response.ok(resultado).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Erro ao buscar estabelecimentos\"}")
                    .build();
        }
    }

    @GET
    @Path("/{id}/estatisticas")
    public Response obterEstatisticas(@PathParam("id") Integer estabelecimentoId, @Context SecurityContext sc) {
        try {
            String emailUsuario = sc != null && sc.getUserPrincipal() != null 
                ? sc.getUserPrincipal().getName() 
                : null;
                
            if (emailUsuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não autenticado\"}")
                        .build();
            }

            var usuarioOpt = usuarioRepository.buscarPorEmail(emailUsuario);
            if (usuarioOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não encontrado\"}")
                        .build();
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar se o usuário é empresário
            if (!usuarioPapelRepository.usuarioTemPapel(usuario.getId(), Papel.CODIGO_EMPRESARIO)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\":\"Acesso permitido apenas para empresários\"}")
                        .build();
            }

            var estabelecimentoOpt = estabelecimentoRepository.buscarAtivoPorId(estabelecimentoId);
            if (estabelecimentoOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Estabelecimento não encontrado\"}")
                        .build();
            }

            Estabelecimento estabelecimento = estabelecimentoOpt.get();

            // Verificar se o usuário é o proprietário
            if (estabelecimento.getProprietario() == null || 
                !estabelecimento.getProprietario().getId().equals(usuario.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\":\"Você não tem permissão para acessar este estabelecimento\"}")
                        .build();
            }

            EstabelecimentoComEstatisticasDTO resultado = converterParaEstatisticas(estabelecimento);
            return Response.ok(resultado).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Erro ao buscar estatísticas\"}")
                    .build();
        }
    }

    private EstabelecimentoComEstatisticasDTO converterParaEstatisticas(Estabelecimento estabelecimento) {
        EstabelecimentoComEstatisticasDTO dto = new EstabelecimentoComEstatisticasDTO();
        dto.setId(estabelecimento.getId());
        dto.setNome(estabelecimento.getNome());
        dto.setLatitude(estabelecimento.getLatitude());
        dto.setLongitude(estabelecimento.getLongitude());
        dto.setEndereco(estabelecimento.getEndereco());
        dto.setAtivo(estabelecimento.getAtivo());
        dto.setCriadoEm(estabelecimento.getCriadoEm());
        
        Long totalCheckins = estabelecimentoRepository.contarCheckinsPorEstabelecimento(estabelecimento.getId());
        dto.setTotalCheckins(totalCheckins != null ? totalCheckins : 0L);
        
        var ultimoCheckin = checkinRepository.buscarUltimoCheckinData(estabelecimento.getId());
        dto.setUltimoCheckin(ultimoCheckin);
        
        return dto;
    }

    private CheckinResponse montarResposta(Checkin checkin) {
        CheckinResponse resp = new CheckinResponse();
        resp.setId(checkin.getId());
        resp.setUsuarioId(checkin.getUsuario() != null ? checkin.getUsuario().getId() : null);
        resp.setEstabelecimentoId(checkin.getEstabelecimento() != null ? checkin.getEstabelecimento().getId() : null);
        resp.setDistanciaMetros(checkin.getDistanciaMetros());
        resp.setCriadoEm(checkin.getCriadoEm());
        return resp;
    }

    private CheckinDetalheDTO converterParaDetalhe(Checkin checkin) {
        CheckinDetalheDTO dto = new CheckinDetalheDTO();
        dto.setId(checkin.getId());
        if (checkin.getUsuario() != null) {
            dto.setUsuarioId(checkin.getUsuario().getId());
            dto.setUsuarioNome(checkin.getUsuario().getNome());
            dto.setUsuarioEmail(checkin.getUsuario().getEmail());
        }
        if (checkin.getEstabelecimento() != null) {
            dto.setEstabelecimentoId(checkin.getEstabelecimento().getId());
        }
        dto.setDistanciaMetros(checkin.getDistanciaMetros());
        dto.setCriadoEm(checkin.getCriadoEm());
        return dto;
    }

    // Cálculo de distância usando fórmula de Haversine
    private double calcularDistanciaMetros(double lat1, double lon1, double lat2, double lon2) {
        final int RAIO_TERRA_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanciaKm = RAIO_TERRA_KM * c;
        return distanciaKm * 1000;
    }
}


