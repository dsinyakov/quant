package io.codera.quant.strategy;

import io.codera.quant.context.TradingContext;
import io.codera.quant.exception.PriceNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Strategy interface.
 */
public interface Strategy {

  Logger log = LoggerFactory.getLogger(Strategy.class);

  /**
   * Executed every time when new data is received.
   * Drives placing trades based on loaded entry and exit criteria and lot sizes.
   * This method will also update {@link BackTestResult object if run in backtest mode}.
   */
  default void onTick() {

    if(isCommonCriteriaMet()) {
      if(isEntryCriteriaMet()) {
        try {
          openPosition();
        } catch (PriceNotAvailableException e) {
          log.error("Price for requested contract is not available");
        }
      } else if(isStopLossCriteriaMet()) {
        try {
          closePosition();
        } catch (PriceNotAvailableException e) {
          log.error("Price for requested contract is not available");
        }
      } else if(isExitCriteriaMet()) {
        try {
          closePosition();
        } catch (PriceNotAvailableException e) {
          log.error("Price for requested contract is not available");
        }
      }
    }
  }

  /**
   * Calculates the lot size based on configured trade context and strategy logic.
   * @param contract instrument/contract name
   * @param buy true of buy, false if sell
   * @return size of the lot
   */
  int getLotSize(String contract, boolean buy);

  /**
   * Checks if common criterion is met for current tick.
   * @return true if met, false otherwise
   */
  boolean isCommonCriteriaMet();

  /**
   * Checks if entry criterion is met for current tick.
   * @return true if met, false otherwise
   */
  boolean isEntryCriteriaMet();

  /**
   * Checks if exit criterion is met for current tick.
   * @return true if met, false otherwise
   */
  boolean isExitCriteriaMet();

  /**
   * Checks if stop loss criterion is met for current tick.
   * @return true if met, false otherwise
   */
  default boolean isStopLossCriteriaMet() {
    return false;
  }

  /**
   * Adds stop loss criterion.
   * @param criterion common criterion
   */
  default void addStopLossCriterion(Criterion criterion) {}

  /**
   * Adds common criterion.
   * @param criterion common criterion
   */
  void addCommonCriterion(Criterion criterion);

  /**
   * Adds entry criterion.
   * @param criterion entry criterion
   */
  void addEntryCriterion(Criterion criterion);

  /**
   * Removes common criterion.
   * @param criterion common criterion
   */
  void removeCommonCriterion(Criterion criterion);

  /**
   * Removes entry criterion.
   * @param criterion entry criterion
   */
  void removeEntryCriterion(Criterion criterion);

  /**
   * Adds exit criterion.
   * @param criterion exit criterion
   */
  void addExitCriterion(Criterion criterion);

  /**
   * Remove exit criterion.
   * @param criterion exit criterion
   */
  void removeExitCriterion(Criterion criterion);

  /**
   * Returns additional data needed for back testing.
   * @return {@link BackTestResult} object
   */
  BackTestResult getBackTestResult();

  /**
   * Add symbol to run strategy against.
   * @param symbol contract symbol
   */
  void addSymbol(String symbol);

  /**
   * Returns strategy {@link TradingContext}
   * @return
   */
  TradingContext getTradingContext();

  /**
   * Opens position in one or several contracts when entry {@link Criterion} is met.
   */
  void openPosition() throws PriceNotAvailableException;

  /**
   * Closes position for contract when exit {@link Criterion} is met.
   */
  void closePosition() throws PriceNotAvailableException;

}
