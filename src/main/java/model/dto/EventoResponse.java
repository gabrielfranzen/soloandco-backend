package model.dto;

import java.util.Date;
import java.util.List;

public class EventoResponse {

    private Integer id;
    private Integer estabelecimentoId;
    private String nome;
    private String dataInicio;
    private String dataFim;
    private String horarioInicio;
    private String horarioFim;
    private String descricao;
    private Boolean entradaGratuita;
    private Boolean ativo;
    private Date criadoEm;
    private List<EventoLinkResponse> links;
    private Integer totalPresencas;
    private Boolean usuarioPretendeIr;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEstabelecimentoId() {
        return estabelecimentoId;
    }

    public void setEstabelecimentoId(Integer estabelecimentoId) {
        this.estabelecimentoId = estabelecimentoId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(String dataInicio) {
        this.dataInicio = dataInicio;
    }

    public String getDataFim() {
        return dataFim;
    }

    public void setDataFim(String dataFim) {
        this.dataFim = dataFim;
    }

    public String getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(String horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public String getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(String horarioFim) {
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

    public List<EventoLinkResponse> getLinks() {
        return links;
    }

    public void setLinks(List<EventoLinkResponse> links) {
        this.links = links;
    }

    public Integer getTotalPresencas() {
        return totalPresencas;
    }

    public void setTotalPresencas(Integer totalPresencas) {
        this.totalPresencas = totalPresencas;
    }

    public Boolean getUsuarioPretendeIr() {
        return usuarioPretendeIr;
    }

    public void setUsuarioPretendeIr(Boolean usuarioPretendeIr) {
        this.usuarioPretendeIr = usuarioPretendeIr;
    }
}
