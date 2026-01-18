package model;

import java.util.Date;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "chat_mensagem", schema = "website")
public class ChatMensagem {

    @Id
    @SequenceGenerator(name = "chat_mensagem_seq", sequenceName = "website.seq_chat_mensagem", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_mensagem_seq")
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id", nullable = false)
    private ChatSala sala;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotBlank(message = "Mensagem é obrigatória")
    @Column(name = "mensagem", length = 1000, nullable = false)
    private String mensagem; // Armazenada criptografada

    @Column(name = "criado_em", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date criadoEm;

    @Column(name = "editado_em")
    @Temporal(TemporalType.TIMESTAMP)
    private Date editadoEm;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ChatSala getSala() {
        return sala;
    }

    public void setSala(ChatSala sala) {
        this.sala = sala;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Date getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Date criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Date getEditadoEm() {
        return editadoEm;
    }

    public void setEditadoEm(Date editadoEm) {
        this.editadoEm = editadoEm;
    }
}

