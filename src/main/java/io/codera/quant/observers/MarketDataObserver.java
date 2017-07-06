package io.codera.quant.observers;

import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.NewTickType;
import rx.Observable;

/**
 *
 */
public interface MarketDataObserver extends ITopMktDataHandler {

  String getSymbol();
  Observable<Price> priceObservable();

  class Price {
    private NewTickType tickType;
    private double price;
    Price(NewTickType tickType, double price) {
      this.tickType = tickType;
      this.price = price;
    }

    public NewTickType getTickType() {
      return tickType;
    }

    public void setTickType(NewTickType tickType) {
      this.tickType = tickType;
    }

    public double getPrice() {
      return price;
    }

    public void setPrice(double price) {
      this.price = price;
    }

  }

}
