package utils;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotAuthorizedExceptionMapper implements ExceptionMapper<NotAuthorizedException> {

    @Override
    public Response toResponse(NotAuthorizedException exception) {
        String mensagem = exception.getMessage();
        
        // Remove o prefixo "HTTP 401 Unauthorized" se existir
        if (mensagem != null && mensagem.contains("HTTP 401")) {
            // Pega só a mensagem customizada
            if (mensagem.contains(":")) {
                String[] partes = mensagem.split(":", 2);
                if (partes.length > 1) {
                    mensagem = partes[1].trim();
                }
            }
        }
        
        // Se a mensagem estiver vazia ou for genérica, usa mensagem padrão
        if (mensagem == null || mensagem.isEmpty() || mensagem.equals("HTTP 401 Unauthorized")) {
            mensagem = "Email ou senha incorretos";
        }
        
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\":\"" + mensagem + "\"}")
                .build();
    }
}

