package model.dto;

import java.util.Date;

public class ChatSalaDetalhadaResponse {
    
    private Integer salaId;
    private Integer estabelecimentoId;
    private String estabelecimentoNome;
    private Date acessoExpiraEm;
    private UltimaMensagemDTO ultimaMensagem;
    private Integer totalParticipantesAtivos;
    private Integer mensagensNaoLidas;

    // Getters e Setters
    public Integer getSalaId() {
        return salaId;
    }

    public void setSalaId(Integer salaId) {
        this.salaId = salaId;
    }

    public Integer getEstabelecimentoId() {
        return estabelecimentoId;
    }

    public void setEstabelecimentoId(Integer estabelecimentoId) {
        this.estabelecimentoId = estabelecimentoId;
    }

    public String getEstabelecimentoNome() {
        return estabelecimentoNome;
    }

    public void setEstabelecimentoNome(String estabelecimentoNome) {
        this.estabelecimentoNome = estabelecimentoNome;
    }

    public Date getAcessoExpiraEm() {
        return acessoExpiraEm;
    }

    public void setAcessoExpiraEm(Date acessoExpiraEm) {
        this.acessoExpiraEm = acessoExpiraEm;
    }

    public UltimaMensagemDTO getUltimaMensagem() {
        return ultimaMensagem;
    }

    public void setUltimaMensagem(UltimaMensagemDTO ultimaMensagem) {
        this.ultimaMensagem = ultimaMensagem;
    }

    public Integer getTotalParticipantesAtivos() {
        return totalParticipantesAtivos;
    }

    public void setTotalParticipantesAtivos(Integer totalParticipantesAtivos) {
        this.totalParticipantesAtivos = totalParticipantesAtivos;
    }

    public Integer getMensagensNaoLidas() {
        return mensagensNaoLidas;
    }

    public void setMensagensNaoLidas(Integer mensagensNaoLidas) {
        this.mensagensNaoLidas = mensagensNaoLidas;
    }

    // Classe interna para última mensagem
    public static class UltimaMensagemDTO {
        private String texto;
        private String usuarioNome;
        private Date criadoEm;

        public String getTexto() {
            return texto;
        }

        public void setTexto(String texto) {
            this.texto = texto;
        }

        public String getUsuarioNome() {
            return usuarioNome;
        }

        public void setUsuarioNome(String usuarioNome) {
            this.usuarioNome = usuarioNome;
        }

        public Date getCriadoEm() {
            return criadoEm;
        }

        public void setCriadoEm(Date criadoEm) {
            this.criadoEm = criadoEm;
        }
    }
}

