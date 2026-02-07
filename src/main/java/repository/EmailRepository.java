package repository;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Stateless
public class EmailRepository {

    @Inject
    @ConfigProperty(name = "email.google.smtp.host", defaultValue = "smtp.gmail.com")
    private String smtpHost;

    @Inject
    @ConfigProperty(name = "email.google.smtp.port", defaultValue = "587")
    private Integer smtpPort;

    @Inject
    @ConfigProperty(name = "email.google.usuario")
    private String usuario;

    @Inject
    @ConfigProperty(name = "email.google.senhaApp")
    private String senhaApp;

    @Inject
    @ConfigProperty(name = "email.google.nomeRemetente", defaultValue = "")
    private String nomeRemetente;

    @Inject
    @ConfigProperty(name = "email.google.tls", defaultValue = "true")
    private Boolean usarTls;

    public void enviarEmail(String destinatario, String assunto, String mensagem, boolean html) {
        validarConfiguracao();

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", usarTls != null && usarTls ? "true" : "false");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.ssl.trust", smtpHost);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, senhaApp);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(obterEnderecoRemetente());
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(assunto, StandardCharsets.UTF_8.name());
            if (html) {
                message.setContent(mensagem, "text/html; charset=UTF-8");
            } else {
                message.setText(mensagem, StandardCharsets.UTF_8.name());
            }
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Falha ao enviar email: " + e.getMessage(), e);
        }
    }

    private void validarConfiguracao() {
        if (usuario == null || usuario.isBlank()) {
            throw new IllegalStateException("Configuração inválida: email.google.usuario");
        }
        if (senhaApp == null || senhaApp.isBlank()) {
            throw new IllegalStateException("Configuração inválida: email.google.senhaApp");
        }
        if (smtpHost == null || smtpHost.isBlank()) {
            throw new IllegalStateException("Configuração inválida: email.google.smtp.host");
        }
        if (smtpPort == null || smtpPort <= 0) {
            throw new IllegalStateException("Configuração inválida: email.google.smtp.port");
        }
    }

    private InternetAddress obterEnderecoRemetente() throws MessagingException {
        try {
            if (nomeRemetente != null && !nomeRemetente.isBlank()) {
                return new InternetAddress(usuario, nomeRemetente, StandardCharsets.UTF_8.name());
            }
            return new InternetAddress(usuario);
        } catch (Exception e) {
            throw new MessagingException("Remetente inválido", e);
        }
    }
}

