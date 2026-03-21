package services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import model.Estabelecimento;
import model.Evento;
import model.EventoLink;
import model.EventoPresenca;
import model.TipoLinkEvento;
import model.Usuario;
import model.dto.EventoLinkRequest;
import model.dto.EventoLinkResponse;
import model.dto.EventoRequest;
import model.dto.EventoResponse;
import model.dto.PresencaEventoResponse;
import repository.EstabelecimentoRepository;
import repository.EventoLinkRepository;
import repository.EventoPresencaRepository;
import repository.EventoRepository;
import repository.TipoLinkEventoRepository;
import repository.UsuarioRepository;

@Path("/estabelecimentos/{estabelecimentoId}/eventos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EventoService {

    private static final String CODIGO_COMPRA_INGRESSO = "compra_de_ingresso";

    @Inject
    private EventoRepository eventoRepository;

    @Inject
    private EventoLinkRepository eventoLinkRepository;

    @Inject
    private EstabelecimentoRepository estabelecimentoRepository;

    @Inject
    private UsuarioRepository usuarioRepository;

    @Inject
    private EventoPresencaRepository eventoPresencaRepository;

    @Inject
    private TipoLinkEventoRepository tipoLinkEventoRepository;

    @GET
    public Response listarEventos(@PathParam("estabelecimentoId") Integer estabelecimentoId,
                                  @Context SecurityContext sc) {
        Optional<Estabelecimento> estabelecimentoOpt = estabelecimentoRepository.buscarAtivoPorId(estabelecimentoId);
        if (estabelecimentoOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Estabelecimento não encontrado\"}")
                    .build();
        }

        Usuario usuarioLogado = resolverUsuario(sc);

        List<Evento> eventos = eventoRepository.listarAtivosPorEstabelecimento(estabelecimentoId);
        List<EventoResponse> resultado = eventos.stream()
                .map(evento -> converterParaResponse(evento, usuarioLogado))
                .collect(Collectors.toList());

        return Response.ok(resultado).build();
    }

    @POST
    public Response criarEvento(@PathParam("estabelecimentoId") Integer estabelecimentoId,
                                EventoRequest request,
                                @Context SecurityContext sc) {
        try {
            Response validacao = validarProprietario(estabelecimentoId, sc);
            if (validacao != null) return validacao;

            Response validacaoCampos = validarCamposEvento(request);
            if (validacaoCampos != null) return validacaoCampos;

            boolean gratuito = request.getEntradaGratuita() == null || request.getEntradaGratuita();
            if (!gratuito) {
                Response validacaoIngresso = validarLinkCompraIngresso(request);
                if (validacaoIngresso != null) return validacaoIngresso;
            }

            Estabelecimento estabelecimento = estabelecimentoRepository.buscarAtivoPorId(estabelecimentoId).get();

            Evento evento = new Evento();
            evento.setEstabelecimento(estabelecimento);
            evento.setNome(request.getNome().trim());
            evento.setDataInicio(LocalDate.parse(request.getDataInicio()));
            evento.setDataFim(LocalDate.parse(request.getDataFim()));
            evento.setHorarioInicio(LocalTime.parse(request.getHorarioInicio()));
            evento.setHorarioFim(LocalTime.parse(request.getHorarioFim()));
            evento.setDescricao(request.getDescricao() != null ? request.getDescricao().trim() : null);
            evento.setEntradaGratuita(gratuito);
            evento.setAtivo(true);

            Evento salvo = eventoRepository.inserir(evento);
            salvarLinks(salvo, request.getLinks());

            EventoResponse response = converterParaResponse(salvo, resolverUsuario(sc));
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Erro ao criar evento: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PUT
    @Path("/{eventoId}")
    public Response atualizarEvento(@PathParam("estabelecimentoId") Integer estabelecimentoId,
                                     @PathParam("eventoId") Integer eventoId,
                                     EventoRequest request,
                                     @Context SecurityContext sc) {
        try {
            Response validacao = validarProprietario(estabelecimentoId, sc);
            if (validacao != null) return validacao;

            Response validacaoCampos = validarCamposEvento(request);
            if (validacaoCampos != null) return validacaoCampos;

            boolean gratuito = request.getEntradaGratuita() == null || request.getEntradaGratuita();
            if (!gratuito) {
                Response validacaoIngresso = validarLinkCompraIngresso(request);
                if (validacaoIngresso != null) return validacaoIngresso;
            }

            Optional<Evento> eventoOpt = eventoRepository.buscarPorId(eventoId);
            if (eventoOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Evento não encontrado\"}")
                        .build();
            }

            Evento evento = eventoOpt.get();
            if (!evento.getEstabelecimento().getId().equals(estabelecimentoId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\":\"Evento não pertence a este estabelecimento\"}")
                        .build();
            }

            evento.setNome(request.getNome().trim());
            evento.setDataInicio(LocalDate.parse(request.getDataInicio()));
            evento.setDataFim(LocalDate.parse(request.getDataFim()));
            evento.setHorarioInicio(LocalTime.parse(request.getHorarioInicio()));
            evento.setHorarioFim(LocalTime.parse(request.getHorarioFim()));
            evento.setDescricao(request.getDescricao() != null ? request.getDescricao().trim() : null);
            evento.setEntradaGratuita(gratuito);

            eventoRepository.atualizar(evento);

            // Se tornou gratuito, remove links de compra de ingresso (frontend já confirmou)
            if (gratuito) {
                eventoLinkRepository.removerPorEventoETipoCodigo(eventoId, CODIGO_COMPRA_INGRESSO);
            }

            eventoLinkRepository.removerPorEvento(eventoId);
            salvarLinks(evento, request.getLinks());

            EventoResponse response = converterParaResponse(evento, resolverUsuario(sc));
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Erro ao atualizar evento: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{eventoId}")
    public Response removerEvento(@PathParam("estabelecimentoId") Integer estabelecimentoId,
                                   @PathParam("eventoId") Integer eventoId,
                                   @Context SecurityContext sc) {
        try {
            Response validacao = validarProprietario(estabelecimentoId, sc);
            if (validacao != null) return validacao;

            Optional<Evento> eventoOpt = eventoRepository.buscarPorId(eventoId);
            if (eventoOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Evento não encontrado\"}")
                        .build();
            }

            Evento evento = eventoOpt.get();
            if (!evento.getEstabelecimento().getId().equals(estabelecimentoId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\":\"Evento não pertence a este estabelecimento\"}")
                        .build();
            }

            evento.setAtivo(false);
            eventoRepository.atualizar(evento);

            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Erro ao remover evento: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/{eventoId}/presenca")
    public Response alternarPresenca(@PathParam("estabelecimentoId") Integer estabelecimentoId,
                                      @PathParam("eventoId") Integer eventoId,
                                      @Context SecurityContext sc) {
        try {
            String emailUsuario = sc != null && sc.getUserPrincipal() != null
                    ? sc.getUserPrincipal().getName()
                    : null;

            if (emailUsuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não autenticado\"}")
                        .build();
            }

            Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorEmail(emailUsuario);
            if (usuarioOpt.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Usuário não encontrado\"}")
                        .build();
            }

            Optional<Evento> eventoOpt = eventoRepository.buscarPorId(eventoId);
            if (eventoOpt.isEmpty() || !Boolean.TRUE.equals(eventoOpt.get().getAtivo())) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Evento não encontrado\"}")
                        .build();
            }

            Evento evento = eventoOpt.get();
            if (!evento.getEstabelecimento().getId().equals(estabelecimentoId)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Evento não encontrado\"}")
                        .build();
            }

            Usuario usuario = usuarioOpt.get();
            Optional<EventoPresenca> presencaExistente =
                    eventoPresencaRepository.buscarPorEventoEUsuario(eventoId, usuario.getId());

            boolean pretendeIr;
            if (presencaExistente.isPresent()) {
                eventoPresencaRepository.removerPorEventoEUsuario(eventoId, usuario.getId());
                pretendeIr = false;
            } else {
                EventoPresenca novaPresenca = new EventoPresenca();
                novaPresenca.setEvento(evento);
                novaPresenca.setUsuario(usuario);
                eventoPresencaRepository.inserir(novaPresenca);
                pretendeIr = true;
            }

            int totalPresencas = (int) eventoPresencaRepository.contarPorEvento(eventoId);
            return Response.ok(new PresencaEventoResponse(pretendeIr, totalPresencas)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Erro ao registrar presença: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    private Response validarLinkCompraIngresso(EventoRequest request) {
        if (request.getLinks() == null || request.getLinks().stream()
                .noneMatch(l -> CODIGO_COMPRA_INGRESSO.equals(l.getTipoCodigo())
                        && l.getUrl() != null && !l.getUrl().isBlank())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Eventos pagos precisam de ao menos um link de compra de ingresso.\"}")
                    .build();
        }
        return null;
    }

    private Response validarProprietario(Integer estabelecimentoId, SecurityContext sc) {
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

        Optional<Estabelecimento> estabelecimentoOpt = estabelecimentoRepository.buscarAtivoPorId(estabelecimentoId);
        if (estabelecimentoOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Estabelecimento não encontrado\"}")
                    .build();
        }

        Estabelecimento estabelecimento = estabelecimentoOpt.get();
        Usuario usuario = usuarioOpt.get();

        if (estabelecimento.getProprietario() == null ||
                !estabelecimento.getProprietario().getId().equals(usuario.getId())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Você não tem permissão para gerenciar eventos deste estabelecimento\"}")
                    .build();
        }

        return null;
    }

    private Response validarCamposEvento(EventoRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Dados do evento são obrigatórios\"}")
                    .build();
        }
        if (request.getNome() == null || request.getNome().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Nome do evento é obrigatório\"}")
                    .build();
        }
        if (request.getDataInicio() == null || request.getDataInicio().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Data de início do evento é obrigatória\"}")
                    .build();
        }
        if (request.getDataFim() == null || request.getDataFim().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Data de fim do evento é obrigatória\"}")
                    .build();
        }
        if (request.getHorarioInicio() == null || request.getHorarioInicio().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Horário de início é obrigatório\"}")
                    .build();
        }
        if (request.getHorarioFim() == null || request.getHorarioFim().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Horário de fim é obrigatório\"}")
                    .build();
        }
        return null;
    }

    private void salvarLinks(Evento evento, List<EventoLinkRequest> links) {
        if (links == null || links.isEmpty()) return;

        for (EventoLinkRequest linkReq : links) {
            if (linkReq.getUrl() == null || linkReq.getUrl().isBlank()) continue;

            TipoLinkEvento tipo = null;
            if (linkReq.getTipoCodigo() != null && !linkReq.getTipoCodigo().isBlank()) {
                tipo = tipoLinkEventoRepository.buscarPorCodigo(linkReq.getTipoCodigo()).orElse(null);
            }

            EventoLink link = new EventoLink();
            link.setEvento(evento);
            link.setTipo(tipo);
            link.setTitulo(linkReq.getTitulo() != null ? linkReq.getTitulo().trim() : null);
            link.setUrl(linkReq.getUrl().trim());
            eventoLinkRepository.inserir(link);
        }
    }

    private Usuario resolverUsuario(SecurityContext sc) {
        if (sc == null || sc.getUserPrincipal() == null) return null;
        String email = sc.getUserPrincipal().getName();
        if (email == null) return null;
        return usuarioRepository.buscarPorEmail(email).orElse(null);
    }

    private EventoResponse converterParaResponse(Evento evento, Usuario usuario) {
        EventoResponse response = new EventoResponse();
        response.setId(evento.getId());
        response.setEstabelecimentoId(evento.getEstabelecimento().getId());
        response.setNome(evento.getNome());
        response.setDataInicio(evento.getDataInicio() != null ? evento.getDataInicio().toString() : null);
        response.setDataFim(evento.getDataFim() != null ? evento.getDataFim().toString() : null);
        response.setHorarioInicio(evento.getHorarioInicio() != null ? evento.getHorarioInicio().toString() : null);
        response.setHorarioFim(evento.getHorarioFim() != null ? evento.getHorarioFim().toString() : null);
        response.setDescricao(evento.getDescricao());
        response.setEntradaGratuita(evento.getEntradaGratuita());
        response.setAtivo(evento.getAtivo());
        response.setCriadoEm(evento.getCriadoEm());

        int totalPresencas = (int) eventoPresencaRepository.contarPorEvento(evento.getId());
        response.setTotalPresencas(totalPresencas);

        if (usuario != null) {
            boolean pretendeIr = eventoPresencaRepository
                    .buscarPorEventoEUsuario(evento.getId(), usuario.getId())
                    .isPresent();
            response.setUsuarioPretendeIr(pretendeIr);
        } else {
            response.setUsuarioPretendeIr(false);
        }

        List<EventoLink> links = eventoLinkRepository.listarPorEvento(evento.getId());
        List<EventoLinkResponse> linksResponse = links.stream().map(link -> {
            EventoLinkResponse lr = new EventoLinkResponse();
            lr.setId(link.getId());
            lr.setTitulo(link.getTitulo());
            lr.setUrl(link.getUrl());
            if (link.getTipo() != null) {
                lr.setTipoId(link.getTipo().getId());
                lr.setTipoCodigo(link.getTipo().getCodigo());
                lr.setTituloTipo(link.getTipo().getNome());
            }
            return lr;
        }).collect(Collectors.toList());

        response.setLinks(linksResponse);
        return response;
    }
}
