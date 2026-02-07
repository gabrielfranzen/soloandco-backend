package model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SolicitarRecuperacaoRequest {

	@NotBlank(message = "E-mail é obrigatório")
	@Email(message = "E-mail deve ser válido")
	private String email;

	@NotBlank(message = "URL do frontend é obrigatória")
	private String frontendUrl;

	public SolicitarRecuperacaoRequest() {
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFrontendUrl() {
		return frontendUrl;
	}

	public void setFrontendUrl(String frontendUrl) {
		this.frontendUrl = frontendUrl;
	}
}

