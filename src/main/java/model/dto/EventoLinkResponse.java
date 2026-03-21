package model.dto;

public class EventoLinkResponse {

    private Integer id;
    private String titulo;
    private String url;
    private Integer tipoId;
    private String tipoCodigo;
    private String tituloTipo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getTipoId() {
        return tipoId;
    }

    public void setTipoId(Integer tipoId) {
        this.tipoId = tipoId;
    }

    public String getTipoCodigo() {
        return tipoCodigo;
    }

    public void setTipoCodigo(String tipoCodigo) {
        this.tipoCodigo = tipoCodigo;
    }

    public String getTituloTipo() {
        return tituloTipo;
    }

    public void setTituloTipo(String tituloTipo) {
        this.tituloTipo = tituloTipo;
    }
}
