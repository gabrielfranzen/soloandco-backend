package model;

import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "chat_sala", schema = "website")
public class ChatSala {

    @Id
    @SequenceGenerator(name = "chat_sala_seq", sequenceName = "website.seq_chat_sala", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_sala_seq")
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estabelecimento_id", nullable = false)
    private Estabelecimento estabelecimento;

    @Column(name = "ativo")
    private Boolean ativo = Boolean.TRUE;

    @Column(name = "criado_em", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date criadoEm;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Estabelecimento getEstabelecimento() {
        return estabelecimento;
    }

    public void setEstabelecimento(Estabelecimento estabelecimento) {
        this.estabelecimento = estabelecimento;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Date getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Date criadoEm) {
        this.criadoEm = criadoEm;
    }
}

