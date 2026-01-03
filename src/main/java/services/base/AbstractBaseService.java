package services.base;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.ejb.EJB;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

public abstract class AbstractBaseService<T> {
	@Context
	private SecurityContext securityContext;

	@Context
	private HttpServletRequest httpHeaders;

	protected String accessToken() {
		String token = this.httpHeaders.getHeader("Authorization").toString();
		return token.replace("Bearer ", "");
	}

	protected Integer getUsuarioIdFromToken() {
		try {
			String token = accessToken();
			DecodedJWT jwt = JWT.decode(token);
			System.out.println("jwt.getClaim(\"id\").asString() => " + jwt.getClaim("id").asString());
			return Integer.parseInt(jwt.getClaim("id").asString());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected String token() {
		return this.httpHeaders.getHeader("Authorization").toString();
	}

	public String getNomeModulo() {

		Class<?> directSubclass = getClass();
		while (directSubclass.getSuperclass() != AbstractBaseService.class) {
			directSubclass = directSubclass.getSuperclass();
		}

		String name = directSubclass != null ? directSubclass.getSimpleName() : getClass().getSimpleName();
		return name.equals("AbstractBaseService") ? "BASE" : name;
	}

	protected String getRequestIpAddress() {
		String[] ipHeaders = { "x-real-ip", "x-forwarded-for" };

		for (String header : ipHeaders) {
			String value = this.httpHeaders.getHeader(header);
			if (value != null) {
				return value;
			}
		}

		return this.httpHeaders.getRemoteAddr();
	}

	

}