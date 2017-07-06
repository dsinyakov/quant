package io.codera.quant.observers;

import com.ib.controller.Bar;
import io.codera.quant.config.ContractBuilder;
import java.time.Instant;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.lst.trading.lib.series.DoubleSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 *
 */
public class IbHistoryObserver implements HistoryObserver {
  private static final Logger logger = LoggerFactory.getLogger(IbHistoryObserver.class);
  private final PublishSubject<DoubleSeries> priceSubject;
  private DoubleSeries doubleSeries;
  private final String symbol;

  public IbHistoryObserver(String symbol) {
    priceSubject = PublishSubject.create();
    this.symbol = symbol;
  }

  @Override
  public void historicalData(Bar bar, boolean hasGaps) {
    if(doubleSeries == null) {
      doubleSeries = new DoubleSeries(this.symbol);
    }

    DateTime dt = new DateTime(bar.time() * 1000);

    if(dt.minuteOfDay().get() >= 390 && dt.minuteOfDay().get() <= 390 + 390) {
      logger.debug("{} {} {}", bar.formattedTime(), symbol, bar.close());

      doubleSeries.add(
          ContractBuilder.getSymbolPrice(symbol, bar.close()),
          Instant.ofEpochMilli(new LocalDateTime(bar.time() * 1000).toDateTime(DateTimeZone.UTC)
              .getMillis()));
    }

  }

  @Override
  public void historicalDataEnd() {
    priceSubject.onNext(doubleSeries);
    logger.debug("End of historic data for " + symbol);
  }

  public Observable<DoubleSeries> observableDoubleSeries() {
    return priceSubject.asObservable();
  }

}
