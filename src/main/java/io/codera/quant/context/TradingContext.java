package io.codera.quant.context;

import io.codera.quant.exception.NoOrderAvailable;
import io.codera.quant.exception.PriceNotAvailableException;
import io.codera.quant.observers.MarketDataObserver;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.lst.trading.lib.model.ClosedOrder;
import org.lst.trading.lib.model.Order;
import org.lst.trading.lib.series.DoubleSeries;
import org.lst.trading.lib.series.TimeSeries;

/**
 * Contains all data needed to run strategy:
 * contract prices, balances etc.
 */
public interface TradingContext {

  /**
   * Returns the time of current tick
   * @return timestamp
   */
  default Instant getTime() {
    return Instant.now();
  }

  /**
   * Returns last price of the contract
   *
   * @param contract contract name
   * @return  last price
   */
  double getLastPrice(String contract) throws PriceNotAvailableException;

  /**
   * Returns history of prices.
   *
   * @param contract contract name
   * @return historical collection of prices
   */
  default Stream<TimeSeries.Entry<Double>> getHistory(String contract) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns history of prices.
   *
   * @param contract contract name
   * @param numberOfDays seconds of history to return before current time instant
   * @return historical collection of prices
   */
  default DoubleSeries getHistory(String contract, int numberOfDays) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns history of prices.
   *
   * @param contract contract name
   * @param numberOfMinutes seconds of history to return before current time instant
   * @return historical collection of prices
   */
  default DoubleSeries getHistoryInMinutes(String contract, int numberOfMinutes) {
    throw new UnsupportedOperationException();
  }

  /**
   * Adds contract into trading contract.
   *
   * @param contract contract name
   */
  void addContract(String contract);

  /**
   * Removes contract from context.
   *
   * @param contract contract name
   */
  void removeContract(String contract);

  /**
   * returns a collection of current contracts in context.
   *
   * @return collection of contracts
   */
  List<String> getContracts();

  /**
   * returns funds currently available for trading.
   *
   * @return funds currently available for trading
   */
  double getAvailableFunds();

  /**
   * Returns cash balance
   * @return cache balance
   */
  double getNetValue();

  /**
   * Returns leverage
   *
   * @return leverage
   */
  double getLeverage();

  /**
   * Returns contract observer
   *
   * @param contractSymbol
   * @return
   */
  default MarketDataObserver getObserver(String contractSymbol){
    throw new UnsupportedOperationException();
  }

  /**
   * Place a contract order
   *
   * @param contractSymbol contract symbol
   * @param buy buy or sell
   * @param amount amount
   * @return {@link Order} object
   */
  Order order(String contractSymbol, boolean buy, int amount) throws PriceNotAvailableException;

  /**
   * Close existing order
   * @param order order to close
   * @return {@link ClosedOrder} object
   */
  ClosedOrder close(Order order) throws PriceNotAvailableException;

  /**
   * Returns last order of th symbol
   *
   * @param symbol contract symbol
   * @return {@link Order} object
   * @throws NoOrderAvailable if no orders available
   */
  Order getLastOrderBySymbol(String symbol) throws NoOrderAvailable;

  /**
   * Returns symbol change if available
   * @param symbol symbol
   * @return symbol change since prior day close
   */
  default double getChangeBySymbol(String symbol) throws PriceNotAvailableException {
    throw new UnsupportedOperationException();
  }

}
