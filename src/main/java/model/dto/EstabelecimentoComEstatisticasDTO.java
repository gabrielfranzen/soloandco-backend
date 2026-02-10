package model.dto;

import java.util.Date;

public class EstabelecimentoComEstatisticasDTO {
    
    private Integer id;
    private String nome;
    private Double latitude;
    private Double longitude;
    private String endereco;
    private Boolean ativo;
    private Date criadoEm;
    private Long totalCheckins;
    private Date ultimoCheckin;

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

    public Long getTotalCheckins() {
        return totalCheckins;
    }

    public void setTotalCheckins(Long totalCheckins) {
        this.totalCheckins = totalCheckins;
    }

    public Date getUltimoCheckin() {
        return ultimoCheckin;
    }

    public void setUltimoCheckin(Date ultimoCheckin) {
        this.ultimoCheckin = ultimoCheckin;
    }
}

