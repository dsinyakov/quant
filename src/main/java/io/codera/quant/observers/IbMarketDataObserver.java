package io.codera.quant.observers;

import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.NewTickType;
import io.codera.quant.config.ContractBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 *  Wraps IB {@link ITopMktDataHandler} into observer to simplify
 *  access to data (price) feed
 */
public class IbMarketDataObserver implements MarketDataObserver {

  private static final Logger log = LoggerFactory.getLogger(IbMarketDataObserver.class);

  private final String symbol;
  private final PublishSubject<Price> priceSubject;

  public IbMarketDataObserver(String symbol) {
    this.symbol = symbol;
    this.priceSubject = PublishSubject.create();
  }

  @Override
  public void tickPrice(NewTickType tickType, double price, int canAutoExecute) {
    if(price == -1.0) { // do not update price with bogus value when market is about ot be closed
      return;
    }
    double realPrice = ContractBuilder.getSymbolPrice(symbol, price);

    priceSubject.onNext(new Price(tickType, realPrice));

  }

  public String getSymbol() {
    return symbol;
  }

  public Observable<Price> priceObservable() {
    return priceSubject.asObservable();
  }
}
