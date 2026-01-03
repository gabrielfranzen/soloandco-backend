package filter;
import utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        final String method = ctx.getMethod();
        final String path = ctx.getUriInfo().getPath();

        // sempre liberar preflight e rotas públicas
        if ("OPTIONS".equalsIgnoreCase(method) || isPublic(method, path)) return;

        String auth = ctx.getHeaderString("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            abort401(ctx);
            return;
        }

        String token = auth.substring("Bearer ".length()).trim();
        try {
        var jws = JwtUtil.parse(token);           
        Claims claims = jws.getPayload();         
        final String email = claims.getSubject();
        final String roles = claims.get("roles", String.class);

        // guarda em propriedades de request para quem quiser ler cru
        ctx.setProperty("userEmail", email);
        ctx.setProperty("roles", roles);

        // injeta SecurityContext para @Context SecurityContext e isUserInRole
        SecurityContext base = ctx.getSecurityContext();
        ctx.setSecurityContext(new SecurityContext() {
            @Override public Principal getUserPrincipal() { return () -> email; }
            @Override public boolean isUserInRole(String role) {
            if (roles == null || roles.isBlank()) return false;
            return Arrays.stream(roles.split(","))
                .map(String::trim)
                .anyMatch(role::equals);
            }
            @Override public boolean isSecure() { return base != null && base.isSecure(); }
            @Override public String getAuthenticationScheme() { return "Bearer"; }
        });

        } catch (Exception e) {
        abort401(ctx);
        }
    }

    private boolean isPublic(String method, String path) {
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        if (normalized.startsWith("api/")) {
            normalized = normalized.substring("api/".length());
        }

        if (normalized.equalsIgnoreCase("auth/login") || normalized.equalsIgnoreCase("auth/refresh")) {
            return true;
        }

        if ("POST".equalsIgnoreCase(method) && normalized.equalsIgnoreCase("usuario")) {
            return true;
        }

        return false;
    }

    private void abort401(ContainerRequestContext ctx) {
        ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
            .entity("{\"error\":\"invalid_or_missing_token\"}")
            .build());
    }
}
