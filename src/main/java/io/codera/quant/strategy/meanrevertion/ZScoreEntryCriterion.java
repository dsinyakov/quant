package io.codera.quant.strategy.meanrevertion;

import io.codera.quant.context.TradingContext;
import io.codera.quant.exception.CriterionViolationException;
import io.codera.quant.exception.PriceNotAvailableException;
import io.codera.quant.strategy.Criterion;

/**
 *
 */
public class ZScoreEntryCriterion implements Criterion {

  private final String firstSymbol;
  private final String secondSymbol;
  private ZScore zScore;
  private TradingContext tradingContext;
  private final double entryZScore;

  public ZScoreEntryCriterion(String firstSymbol, String secondSymbol,
                              double entryZScore, ZScore zScore, TradingContext tradingContext) {
    this.zScore = zScore;
    this.firstSymbol = firstSymbol;
    this.secondSymbol = secondSymbol;
    this.tradingContext = tradingContext;
    this.entryZScore = entryZScore;
  }

  @Override
  public boolean isMet() throws CriterionViolationException {
    try {
      double zs =
          zScore.get(
              tradingContext.getLastPrice(firstSymbol), tradingContext.getLastPrice(secondSymbol));
      if(zs < -entryZScore || zs > entryZScore) {
        return true;
      }
    } catch (PriceNotAvailableException e) {
      return false;
    }
    return false;
  }
}
