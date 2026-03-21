package model;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_link_evento", schema = "website")
public class TipoLinkEvento {

    @Id
    @SequenceGenerator(name = "tipo_link_evento_seq", sequenceName = "website.seq_tipo_link_evento", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tipo_link_evento_seq")
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 50, nullable = false, unique = true)
    private String codigo;

    @Column(name = "nome", length = 150, nullable = false)
    private String nome;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
