package io.codera.quant.observers;

import io.codera.quant.context.TradingContext;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 *
 */
public class IbAccountObserver implements AccountObserver {

  private final PublishSubject<Double> cashBalanceSubject;
  private final PublishSubject<Double> netValueSubject;
  private final TradingContext tradingContext;

  public IbAccountObserver(TradingContext tradingContext) {
    cashBalanceSubject = PublishSubject.create();
    netValueSubject = PublishSubject.create();
    this.tradingContext = tradingContext;
  }

  @Override
  public void setCashBalance(double balance) {
    cashBalanceSubject.onNext(balance);
  }

  @Override
  public void setNetValue(double netValue) {
    logger.debug("Setting net value");
    netValueSubject.onNext(netValue);
  }

  @Override
  public void updateSymbolPosition(String symbol, double position) {
    logger.info("{} position: {}", symbol, position);

  }

  public Observable<Double> observableCashBalance() {
    return cashBalanceSubject.asObservable();
  }

  public Observable<Double> observableNetValue() {
    return netValueSubject.asObservable();
  }
}
