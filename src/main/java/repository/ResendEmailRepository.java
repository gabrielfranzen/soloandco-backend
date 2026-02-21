package repository;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import com.resend.services.emails.model.SendEmailResponse;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Stateless
public class ResendEmailRepository {

    @Inject
    @ConfigProperty(name = "email.resend.apiKey")
    private String apiKey;

    @Inject
    @ConfigProperty(name = "email.resend.remetente")
    private String remetente;

    @Inject
    @ConfigProperty(name = "email.resend.nomeRemetente", defaultValue = "Solo & Co")
    private String nomeRemetente;

    public void enviarEmail(String destinatario, String assunto, String mensagem, boolean html) {
        validarConfiguracao();

        try {
            // Inicializa o cliente Resend
            Resend resend = new Resend(apiKey);

            // Monta o remetente com nome se fornecido
            String remetenteCompleto = nomeRemetente != null && !nomeRemetente.isBlank() 
                ? nomeRemetente + " <" + remetente + ">" 
                : remetente;

            // Cria a requisição usando o builder do SDK
            SendEmailRequest.Builder requestBuilder = SendEmailRequest.builder()
                .from(remetenteCompleto)
                .to(destinatario)
                .subject(assunto);

            // Adiciona conteúdo HTML ou texto
            if (html) {
                requestBuilder.html(mensagem);
            } else {
                requestBuilder.text(mensagem);
            }

            SendEmailRequest sendEmailRequest = requestBuilder.build();

            // Envia o email usando o SDK
            SendEmailResponse response = resend.emails().send(sendEmailRequest);

            // Verifica se houve erro na resposta
            if (response == null) {
                throw new RuntimeException("Resposta vazia ao enviar email via Resend");
            }

        } catch (ResendException e) {
            throw new RuntimeException("Falha ao enviar email via Resend: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erro inesperado ao enviar email: " + e.getMessage(), e);
        }
    }

    private void validarConfiguracao() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Configuração inválida: email.resend.apiKey");
        }
        if (remetente == null || remetente.isBlank()) {
            throw new IllegalStateException("Configuração inválida: email.resend.remetente");
        }
    }
}

