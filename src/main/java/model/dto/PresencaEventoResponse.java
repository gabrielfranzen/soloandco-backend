package model.dto;

public class PresencaEventoResponse {

    private Boolean pretendeIr;
    private Integer totalPresencas;

    public PresencaEventoResponse(Boolean pretendeIr, Integer totalPresencas) {
        this.pretendeIr = pretendeIr;
        this.totalPresencas = totalPresencas;
    }

    public Boolean getPretendeIr() {
        return pretendeIr;
    }

    public void setPretendeIr(Boolean pretendeIr) {
        this.pretendeIr = pretendeIr;
    }

    public Integer getTotalPresencas() {
        return totalPresencas;
    }

    public void setTotalPresencas(Integer totalPresencas) {
        this.totalPresencas = totalPresencas;
    }
}
