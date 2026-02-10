package model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "estabelecimento", schema = "website")
public class Estabelecimento {

    @Id
    @SequenceGenerator(name = "estabelecimento_seq", sequenceName = "website.seq_estabelecimento", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "estabelecimento_seq")
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(name = "nome", length = 150, nullable = false)
    private String nome;

    @NotNull(message = "Latitude é obrigatória")
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @NotNull(message = "Longitude é obrigatória")
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "endereco", length = 255)
    private String endereco;

    @Column(name = "ativo")
    private Boolean ativo = Boolean.TRUE;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_proprietario_id")
    private Usuario proprietario;

    @Column(name = "criado_em", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date criadoEm;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
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

    public Usuario getProprietario() {
        return proprietario;
    }

    public void setProprietario(Usuario proprietario) {
        this.proprietario = proprietario;
    }
}


