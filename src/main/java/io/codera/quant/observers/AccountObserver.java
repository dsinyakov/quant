package io.codera.quant.observers;

import com.ib.controller.ApiController.IAccountHandler;
import com.ib.controller.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public interface AccountObserver extends IAccountHandler {

  Logger logger = LoggerFactory.getLogger(AccountObserver.class);

  default void accountValue(String account, String key, String value, String currency) {
    if(key.equals("NetLiquidation") && currency.equals("USD")) {
      logger.debug(String.format("account: %s, key: %s, value: %s, currency: %s",
          account, key, value, currency));
      setNetValue(Double.valueOf(value));
    }
    if(key.equals("AvailableFunds") && currency.equals("USD")) {
      logger.debug(String.format("account: %s, key: %s, value: %s, currency: %s",
          account, key, value, currency));
      setCashBalance(Double.valueOf(value));
    }
  }

  default void accountTime(String timeStamp) {
    logger.debug(String.format("account time: %s", timeStamp));
  }

  default void accountDownloadEnd(String account) {
    logger.debug(String.format("account download end: %s", account));
  }

  @Override
  default void updatePortfolio(Position position) {
    updateSymbolPosition(position.contract().symbol(), position.position());
  }

  void setCashBalance(double balance);
  void setNetValue(double netValue);
  void updateSymbolPosition(String symbol, int position);

}
