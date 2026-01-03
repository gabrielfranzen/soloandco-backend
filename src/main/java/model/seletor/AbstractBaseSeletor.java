package model.seletor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import repository.base.BaseQuery;

public abstract class AbstractBaseSeletor {

    public static final String ORDER_ASC = "asc";
    public static final String ORDER_DESC = "desc";

    private Integer limite;
    private Integer pagina;

    private String orderField;
    private String orderType;

    //*****************************************************************
    //PAGINACAO
    //*****************************************************************

    @JsonIgnore
    public Integer getLimit() {
        return this.limite;
    }

    @JsonIgnore
    public Integer getOffset() {
        if (this.limite != null && this.pagina != null) {
            return ( this.limite * ( this.pagina - 1) );
        }
        else {
            return null;
        }
    }

    public void setLimite( Integer limite ) {
        this.limite = limite;
    }
    public void setPagina( Integer pagina ) {
        this.pagina = pagina;
    }
    public Integer getLimite() {
        return limite;
    }

    public Integer getPagina() {
        return pagina;
    }

    //*****************************************************************
    //ORDENACAO
    //*****************************************************************



    @JsonIgnore
    public boolean hasOrderRule(){
        return ( this.orderField != null && this.orderField.length() > 0 );
    }

    public String getOrderField() {
        return orderField;
    }

    public void setOrderField(String orderField) {
        this.orderField = orderField;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public void aplicarFiltroBase(BaseQuery<?> query) {
        query.setMaxResults(this.getLimite())
                .setFirstResult(this.getOffset())
                .addOrderBy(this.getOrderType(), this.getOrderField());
    }
}
