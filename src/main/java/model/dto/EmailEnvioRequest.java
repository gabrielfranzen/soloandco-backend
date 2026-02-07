package model.dto;

public class EmailEnvioRequest {

    private String destinatario;
    private String assunto;
    private String mensagem;
    private Boolean html;

    public EmailEnvioRequest() {
        // Construtor padrão necessário para JSON Binding
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Boolean getHtml() {
        return html;
    }

    public void setHtml(Boolean html) {
        this.html = html;
    }
}


