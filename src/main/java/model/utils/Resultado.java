package model.utils;

import java.util.ArrayList;
import java.util.List;

public class Resultado<T> {
   private List<T> itens;
   private Long total;

   public Resultado() {
   }

   public Resultado(List<T> itens, Long total) {
      this.itens = itens;
      this.total = total;
   }

   public List<?> getItens() {
      return this.itens;
   }

   public void setItens(List<T> itens) {
      this.itens = itens;
   }

   public Long getTotal() {
      return this.total;
   }

   public void setTotal(Long total) {
      this.total = total;
   }

   public void addItem(T item) {
      if (this.itens == null) {
         this.itens = new ArrayList();
      }

      this.itens.add(item);
   }
}