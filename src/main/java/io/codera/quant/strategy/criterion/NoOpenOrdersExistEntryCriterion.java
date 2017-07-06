package io.codera.quant.strategy.criterion;

import io.codera.quant.context.TradingContext;
import io.codera.quant.exception.CriterionViolationException;
import io.codera.quant.exception.NoOrderAvailable;
import io.codera.quant.strategy.Criterion;
import java.util.List;
import org.lst.trading.lib.model.Order;


/**
 * Checks that no open orders available for specified symbols
 */
public class NoOpenOrdersExistEntryCriterion implements Criterion {

  protected final List<String> symbols;
  protected final TradingContext tradingContext;

  public NoOpenOrdersExistEntryCriterion(TradingContext tradingContext, List<String> symbols) {
    this.tradingContext = tradingContext;
    this.symbols = symbols;
  }

  @Override
  public boolean isMet() throws CriterionViolationException {
    for(String symbol : symbols) {
      try {
        Order order = tradingContext.getLastOrderBySymbol(symbol);
        if(order != null) {
          return false;
        }
      } catch (NoOrderAvailable ignored) {
      }
    }
    return true;
  }
}