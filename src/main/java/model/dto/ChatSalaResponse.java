package model.dto;

import java.util.Date;

public class ChatSalaResponse {
    
    private Integer id;
    private Integer estabelecimentoId;
    private String estabelecimentoNome;
    private Boolean ativo;
    private Date criadoEm;
    private Date acessoExpiraEm; // Do participante

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

    public String getEstabelecimentoNome() {
        return estabelecimentoNome;
    }

    public void setEstabelecimentoNome(String estabelecimentoNome) {
        this.estabelecimentoNome = estabelecimentoNome;
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

    public Date getAcessoExpiraEm() {
        return acessoExpiraEm;
    }

    public void setAcessoExpiraEm(Date acessoExpiraEm) {
        this.acessoExpiraEm = acessoExpiraEm;
    }
}

