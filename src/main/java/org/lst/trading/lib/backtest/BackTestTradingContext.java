package org.lst.trading.lib.backtest;

import com.google.common.collect.Maps;
import io.codera.quant.context.TradingContext;
import io.codera.quant.exception.NoOrderAvailable;
import io.codera.quant.exception.PriceNotAvailableException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;
import org.lst.trading.lib.model.ClosedOrder;
import org.lst.trading.lib.model.Order;
import org.lst.trading.lib.series.DoubleSeries;
import org.lst.trading.lib.series.MultipleDoubleSeries;
import org.lst.trading.lib.series.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static org.lst.trading.lib.util.Util.check;

public class BackTestTradingContext implements TradingContext {
  Instant mInstant;
  List<Double> mPrices;
  List<String> mInstruments;
  DoubleSeries mPl = new DoubleSeries("pl");
  DoubleSeries mFundsHistory = new DoubleSeries("funds");
  MultipleDoubleSeries mHistory;
  double mInitialFunds;
  double mCommissions;
  private Map<String, Order> orders;
  private Map<String, Double> closePriceMap = Maps.newConcurrentMap();

  int mOrderId = 1;

  List<SimpleOrder> mOrders = new ArrayList<>();

  double mClosedPl = 0;
  List<SimpleClosedOrder> mClosedOrders = new ArrayList<>();
  double mLeverage;
  private static Logger logger = LoggerFactory.getLogger(BackTestTradingContext.class);

  @Override public Instant getTime() {
    return mInstant;
  }

  @Override public double getLastPrice(String instrument) {
    logger.info("Time: {}", mInstant.toString());
    
    Date date = Date.from(mInstant);
    SimpleDateFormat hourMinutes = new SimpleDateFormat("HH:mm");
    hourMinutes.setTimeZone(TimeZone.getTimeZone("UTC"));
    String formattedHourMinutes = hourMinutes.format(date);
    double price = mPrices.get(mInstruments.indexOf(instrument));
    if(formattedHourMinutes.equals("13:00")) {
      closePriceMap.put(instrument, price);
    }
    return price;
  }

  @Override public Stream<TimeSeries.Entry<Double>> getHistory(String instrument) {
    int index = mInstruments.indexOf(instrument);
    return mHistory.reversedStream().map(t -> new TimeSeries.Entry<>(t.getItem().get(index), t.getInstant()));
  }

  @Override
  public void addContract(String contract) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeContract(String contract) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> getContracts() {
    return mInstruments;
  }

  @Override public Order order(String instrument, boolean buy, int amount) {
//    check(amount > 0);
    logger.info("OPEN {} in amount {}", instrument, (buy ? 1 : -1) * amount);
    double price = getLastPrice(instrument);
    SimpleOrder order = new SimpleOrder(mOrderId++, instrument, getTime(), price, amount * (buy ? 1 : -1));
    mOrders.add(order);
    if(orders == null) {
      orders = Maps.newConcurrentMap();
    }
    orders.put(instrument, order);

    mCommissions += calculateCommission(order);

    return order;
  }

  @Override public ClosedOrder close(Order order) {
    logger.info("CLOSE {} in amount {}", order.getInstrument(), -order.getAmount());

    SimpleOrder simpleOrder = (SimpleOrder) order;
    mOrders.remove(simpleOrder);
    double price = getLastPrice(order.getInstrument());
    SimpleClosedOrder closedOrder = new SimpleClosedOrder(simpleOrder, price, getTime());
    mClosedOrders.add(closedOrder);
    mClosedPl += closedOrder.getPl();
    mCommissions += calculateCommission(order);
    if(orders != null) {
      orders.remove(order.getInstrument());
    }

    return closedOrder;
  }

  @Override
  public Order getLastOrderBySymbol(String symbol) throws NoOrderAvailable {
    checkArgument(symbol != null, "symbol is null");
    if(orders == null || !orders.containsKey(symbol)) {
      throw new NoOrderAvailable();
    }
    return orders.get(symbol);
  }

  public double getPl() {
    return mClosedPl + mOrders.stream().mapToDouble(t -> t.calculatePl(getLastPrice(t.getInstrument()))).sum() - mCommissions;
  }


  @Override public double getAvailableFunds() {
    return getNetValue() - mOrders.stream().mapToDouble(t -> Math.abs(t.getAmount()) * t.getOpenPrice() / mLeverage).sum();
  }

  public double getInitialFunds() {
    return mInitialFunds;
  }

  @Override public double getNetValue() {
    return mInitialFunds + getPl();
  }

  @Override public double getLeverage() {
    return mLeverage;
  }

  double calculateCommission(Order order) {
    if(order.getInstrument().contains("/")) {
      return Math.abs(order.getAmount()) * order.getOpenPrice() * 0.00002;
    }
    else if(order.getInstrument().contains("=F")) {
      return Math.abs(order.getAmount()) * 2.04;
    }
    double commissions = Math.max(1,  Math.abs(order.getAmount()) * 0.005);
    logger.debug("Commissions: {}", commissions);
    return commissions;
  }

  public double getChangeBySymbol(String symbol) throws PriceNotAvailableException {

    if(!closePriceMap.containsKey(symbol)) {
      throw new PriceNotAvailableException();
    }
    double closePrice = closePriceMap.get(symbol);
    double currentPrice = getLastPrice(symbol);

    BigDecimal diff = BigDecimal.valueOf(currentPrice).add(BigDecimal.valueOf(-closePrice));

    BigDecimal res = diff.multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(closePrice), RoundingMode.HALF_UP);
    BigDecimal rounded = res.setScale(2, RoundingMode.HALF_UP);
    return rounded.doubleValue();
  }
}

