package io.codera.quant.observers;

import com.ib.client.TickType;
import com.ib.client.Types;
import com.ib.controller.ApiController.ITopMktDataHandler;
import rx.Observable;

/**
 *
 */
public interface MarketDataObserver extends ITopMktDataHandler {

  String getSymbol();
  Observable<Price> priceObservable();

  @Override
  default void tickSize(TickType tickType, int size) {}

  @Override
  default void tickString(TickType tickType, String value) {}

  @Override
  default void tickSnapshotEnd() {}

  @Override
  default void marketDataType(Types.MktDataType marketDataType) {}

  class Price {
    private TickType tickType;
    private double price;
    Price(TickType tickType, double price) {
      this.tickType = tickType;
      this.price = price;
    }

    public TickType getTickType() {
      return tickType;
    }

    public void setTickType(TickType tickType) {
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
