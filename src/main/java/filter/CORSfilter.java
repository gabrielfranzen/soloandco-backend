package filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.util.Set;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.Provider;


@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 10) // roda antes do filtro de auth
public class CORSfilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String ALLOW_HEADERS = "Origin, Content-Type, Accept, Authorization";
    private static final String ALLOW_METHODS = "GET, POST, PUT, PATCH, DELETE, OPTIONS";
    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:8182",
            "http://127.0.0.1:8182"
    );

    private String resolveOrigin(ContainerRequestContext request) {
        String origin = request.getHeaderString("Origin");
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            return origin;
        }
        return null;
    }

    @Override
    public void filter(ContainerRequestContext request) throws IOException {
        // libera preflight sem passar pelo resto da stack
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        String origin = resolveOrigin(request);
        Response.ResponseBuilder respBuilder = Response.noContent()
            .header("Access-Control-Allow-Headers", ALLOW_HEADERS)
            .header("Access-Control-Allow-Methods", ALLOW_METHODS)
            .header("Access-Control-Allow-Credentials", "true");
        if (origin != null) {
            respBuilder.header("Access-Control-Allow-Origin", origin);
        }
        Response resp = respBuilder.build();
        request.abortWith(resp);
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        String origin = resolveOrigin(request);
        if (origin != null) {
            response.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
            response.getHeaders().putSingle("Vary", "Origin");
        }
        response.getHeaders().putSingle("Access-Control-Allow-Headers", ALLOW_HEADERS);
        response.getHeaders().putSingle("Access-Control-Allow-Methods", ALLOW_METHODS);
        response.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        // útil para ler cabeçalhos do lado do browser (downloads, etc.)
        response.getHeaders().putSingle("Access-Control-Expose-Headers", "Location, Content-Disposition");
    }
}


