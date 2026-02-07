package services;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.dto.EmailEnvioRequest;
import repository.EmailRepository;

@Path("/email")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EmailService {

    @Inject
    private EmailRepository emailRepository;

    @POST
    @Path("/enviar")
    public Response enviarEmail(EmailEnvioRequest dados) {
        try {
            if (dados == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Dados do email são obrigatórios").build();
            }
            if (dados.getDestinatario() == null || dados.getDestinatario().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Destinatário é obrigatório").build();
            }
            if (dados.getAssunto() == null || dados.getAssunto().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Assunto é obrigatório").build();
            }
            if (dados.getMensagem() == null || dados.getMensagem().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Mensagem é obrigatória").build();
            }

            emailRepository.enviarEmail(
                dados.getDestinatario().trim(),
                dados.getAssunto().trim(),
                dados.getMensagem(),
                Boolean.TRUE.equals(dados.getHtml())
            );

            return Response.ok().entity("Email enviado com sucesso").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erro ao enviar email: " + e.getMessage())
                .build();
        }
    }
}


