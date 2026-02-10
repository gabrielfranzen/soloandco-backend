package model;

import java.util.Date;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "papeis", schema = "website")
public class Papel {

    // Códigos dos papéis (constantes para uso no sistema)
    public static final String CODIGO_USUARIO = "usuario";
    public static final String CODIGO_EMPRESARIO = "empresario";

    @Id
    @SequenceGenerator(name = "papeis_seq", sequenceName = "website.seq_papeis", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "papeis_seq")
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    @NotBlank(message = "Código é obrigatório")
    @Column(name = "codigo", length = 50, nullable = false, unique = true)
    private String codigo;

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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Date getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Date criadoEm) {
        this.criadoEm = criadoEm;
    }
}

