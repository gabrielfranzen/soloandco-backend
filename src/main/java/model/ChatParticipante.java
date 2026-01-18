package model;

import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "chat_participante", schema = "website")
public class ChatParticipante {

    @Id
    @SequenceGenerator(name = "chat_participante_seq", sequenceName = "website.seq_chat_participante", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_participante_seq")
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id", nullable = false)
    private ChatSala sala;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkin_id", nullable = false)
    private Checkin checkin;

    @Column(name = "acesso_expira_em", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date acessoExpiraEm;

    @Column(name = "criado_em", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date criadoEm;

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

    public Checkin getCheckin() {
        return checkin;
    }

    public void setCheckin(Checkin checkin) {
        this.checkin = checkin;
    }

    public Date getAcessoExpiraEm() {
        return acessoExpiraEm;
    }

    public void setAcessoExpiraEm(Date acessoExpiraEm) {
        this.acessoExpiraEm = acessoExpiraEm;
    }

    public Date getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Date criadoEm) {
        this.criadoEm = criadoEm;
    }
}

