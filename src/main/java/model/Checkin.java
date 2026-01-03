package model;

import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "checkin", schema = "website")
public class Checkin {

    @Id
    @SequenceGenerator(name = "checkin_seq", sequenceName = "website.seq_checkin", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "checkin_seq")
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estabelecimento_id", nullable = false)
    private Estabelecimento estabelecimento;

    @Column(name = "distancia_m")
    private Double distanciaMetros;

    @Column(name = "criado_em", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date criadoEm;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Estabelecimento getEstabelecimento() {
        return estabelecimento;
    }

    public void setEstabelecimento(Estabelecimento estabelecimento) {
        this.estabelecimento = estabelecimento;
    }

    public Double getDistanciaMetros() {
        return distanciaMetros;
    }

    public void setDistanciaMetros(Double distanciaMetros) {
        this.distanciaMetros = distanciaMetros;
    }

    public Date getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Date criadoEm) {
        this.criadoEm = criadoEm;
    }
}


