package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "evento", schema = "website")
public class Evento {

    @Id
    @SequenceGenerator(name = "evento_seq", sequenceName = "website.seq_evento", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evento_seq")
    @Column(name = "id", nullable = false)
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estabelecimento_id", nullable = false)
    private Estabelecimento estabelecimento;

    @NotBlank(message = "Nome é obrigatório")
    @Column(name = "nome", length = 200, nullable = false)
    private String nome;

    @NotNull(message = "Data de início é obrigatória")
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @NotNull(message = "Horário de início é obrigatório")
    @Column(name = "horario_inicio", nullable = false)
    private LocalTime horarioInicio;

    @NotNull(message = "Horário de fim é obrigatório")
    @Column(name = "horario_fim", nullable = false)
    private LocalTime horarioFim;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "entrada_gratuita", nullable = false)
    private Boolean entradaGratuita = Boolean.TRUE;

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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public LocalTime getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(LocalTime horarioFim) {
        this.horarioFim = horarioFim;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getEntradaGratuita() {
        return entradaGratuita;
    }

    public void setEntradaGratuita(Boolean entradaGratuita) {
        this.entradaGratuita = entradaGratuita;
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
