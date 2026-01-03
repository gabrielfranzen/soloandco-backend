package services;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import model.dto.LoginRequest;
import model.dto.RefreshRequest;
import repository.AuthRepository;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthService {

    @EJB
    private AuthRepository authRepository;

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
    
    
    


}
