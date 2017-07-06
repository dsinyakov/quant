package io.codera.quant.strategy.kalman;

import io.codera.quant.config.ContractBuilder;
import io.codera.quant.context.IbTradingContext;
import io.codera.quant.context.TradingContext;
import io.codera.quant.exception.CriterionViolationException;
import io.codera.quant.exception.NoOrderAvailable;
import io.codera.quant.exception.PriceNotAvailableException;
import io.codera.quant.strategy.AbstractStrategy;
import io.codera.quant.strategy.Criterion;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.commons.math3.stat.StatUtils;
import org.lst.trading.lib.series.DoubleSeries;
import org.lst.trading.lib.series.MultipleDoubleSeries;
import org.lst.trading.lib.series.TimeSeries;
import org.lst.trading.main.strategy.kalman.Cointegration;

/**
 * Kalman filter strategy
 */
public class KalmanFilterStrategy extends AbstractStrategy {

  private final String firstSymbol;
  private final String secondSymbol;
  private final Cointegration cointegration;
  private double beta;
  private double baseAmount;
  private double sd;
  private ErrorIsMoreThanStandardDeviationEntry mainEntry;
  private KalmanFilterExitCriterion mainExit;

  public KalmanFilterStrategy(String firstSymbol, String secondSymbol,
                              TradingContext tradingContext, Cointegration cointegration) {
    super(tradingContext);
    this.firstSymbol = firstSymbol;
    this.secondSymbol = secondSymbol;
    this.cointegration = cointegration;
    this.mainEntry = new ErrorIsMoreThanStandardDeviationEntry();
    this.mainExit =  new KalmanFilterExitCriterion();
    addEntryCriterion(mainEntry);
    addExitCriterion(mainExit);
  }

  public void setErrorQueueSize(int size) {
    mainEntry.setErrorQueueSize(size);
  }

  public void setEntrySdMultiplier(double multiplier) {
    mainEntry.setSdMultiplier(multiplier);
  }

  public void setExitMultiplier(double multiplier) {
    mainExit.setSdMultiplier(multiplier);
  }

  public void openPosition() throws PriceNotAvailableException {

    tradingContext.order(secondSymbol, cointegration.getError() < 0, (int) baseAmount);
    log.debug("Order of {} in amount {}", secondSymbol, (int)baseAmount);

    tradingContext.order(firstSymbol, cointegration.getError() > 0, (int) (baseAmount * beta));
    log.debug("Order of {} in amount {}", firstSymbol, (int) (baseAmount * beta));
  }

  public void closePosition() throws PriceNotAvailableException {
    try {
      tradingContext.close(tradingContext.getLastOrderBySymbol(firstSymbol));
    } catch (NoOrderAvailable noOrderAvailable) {
      log.error("No order available for {}", firstSymbol);
    }
    try {
      tradingContext.close(tradingContext.getLastOrderBySymbol(secondSymbol));
    } catch (NoOrderAvailable noOrderAvailable) {
      log.error("No order available for {}", secondSymbol);
    }
  }

  @Override
  public int getLotSize(String contract, boolean buy) {
    throw new UnsupportedOperationException();
  }

  public class ErrorIsMoreThanStandardDeviationEntry implements Criterion {

    private Queue<Double> errorQueue = new ConcurrentLinkedQueue<>();
    private static final int ERROR_QUEUE_SIZE_DEFAULT = 30;

    public void setSdMultiplier(double sdMultiplier) {
      this.sdMultiplier = sdMultiplier;
    }

    private double sdMultiplier;

    public void setErrorQueueSize(int errorQueueSize) {
      this.errorQueueSize = errorQueueSize;
    }

    private int errorQueueSize;


    public ErrorIsMoreThanStandardDeviationEntry() {
      errorQueueSize = ERROR_QUEUE_SIZE_DEFAULT;
      sdMultiplier = 1;
    }

    @Override
    public void init() {
      if(tradingContext instanceof IbTradingContext) {

        DoubleSeries firstSymbolHistory =
            tradingContext.getHistory(firstSymbol, 2);

        DoubleSeries secondSymbolHistory =
            tradingContext.getHistory(secondSymbol, 2);

        MultipleDoubleSeries multipleDoubleSeries = new MultipleDoubleSeries(firstSymbolHistory,
            secondSymbolHistory);

        for (TimeSeries.Entry<List<Double>> entry : multipleDoubleSeries) {
          // TODO (dsinyakov): remove cointegration logic duplication below

          double x = entry.getItem().get(0);
          double y = entry.getItem().get(1);
          if(firstSymbol.contains("=F") && secondSymbol.contains("=F")) {
            x = x * ContractBuilder.getFutureMultiplier(firstSymbol);
            y = y * ContractBuilder.getFutureMultiplier(secondSymbol);
          }
          cointegration.step(x, y);

          double error = cointegration.getError();
          errorQueue.add(error);
          if (errorQueue.size() > errorQueueSize + 1) {
            errorQueue.poll();
          }
        }
      }
    }

    @Override
    public boolean isMet() throws CriterionViolationException {
      log.debug("Evaluating ErrorIsMoreThanStandardDeviationEntry criteria");
      double x;
      try {
        x = tradingContext.getLastPrice(firstSymbol);
        log.info("Current {} price {}", firstSymbol, x);

      } catch (PriceNotAvailableException e) {
        log.error("Price for " + firstSymbol + " is not available.");
        return false;
      }
      double y;
      try {
        y = tradingContext.getLastPrice(secondSymbol);
        log.info("Current {} price {}", secondSymbol, y);
      } catch (PriceNotAvailableException e) {
        log.error("Price for " + secondSymbol + " is not available.");
        return false;
      }
      beta = cointegration.getBeta();

      if(firstSymbol.contains("=F") && secondSymbol.contains("=F")) {
         x = x * ContractBuilder.getFutureMultiplier(firstSymbol);
         y = y * ContractBuilder.getFutureMultiplier(secondSymbol);
      }
      cointegration.step(x, y);

      double error = cointegration.getError();
      errorQueue.add(error);
      log.debug("Error Queue size: {}", errorQueue.size());
      if (errorQueue.size() > errorQueueSize + 1) {
        errorQueue.poll();
      }

      if (errorQueue.size() > errorQueueSize) {
        log.debug("Kalman filter queue is > " + errorQueueSize);
        Object[] errors = errorQueue.toArray();
        double[] lastValues = new double[errorQueueSize/2];

        for (int i = errors.length - 1, lastValIndex = 0;
             i > errors.length - 1 - errorQueueSize/2;
             i--, lastValIndex++) {
          lastValues[lastValIndex] = Double.valueOf(errors[i].toString());
        }

        sd = Math.sqrt(StatUtils.variance(lastValues));
        double realSd = sdMultiplier * sd;
        log.info("error={}, sd={}", error, realSd);
        if (Math.abs(error) > realSd) {
          log.debug("error is bigger than square root of standard deviation");
          log.debug("Net value {}", tradingContext.getNetValue());
          if(secondSymbol.contains("=F")) {
            //Exchange	Underlying	Product description	Trading Class	Intraday Initial 1	Intraday Maintenance 1	Overnight Initial	Overnight Maintenance	Currency	Has Options
            //GLOBEX	ES	E-mini S&P 500	                          ES	3665	    2932	7330	5864	USD
            // 	Yes
            //ECBOT	YM	Mini Sized Dow Jones Industrial Average $5	YM	3218.125	2574.50	6436.25	5149	USD	Yes

            baseAmount = 4;
            beta = 1;
          } else {
            baseAmount =
                (tradingContext.getNetValue() * 0.5 * Math.min(4, tradingContext.getLeverage()))
                    / (y + beta * x);
            log.debug("baseAmount={},  sd={}, beta={}", baseAmount, cointegration.getError(), beta);

          }
          if (beta > 0 && baseAmount * beta >= 1) {
            log.info("error={}, sd={}", error, realSd);
            log.info("{} price {}; {} price {}", firstSymbol, x, secondSymbol, y);
            return true;

          }

        }
      }
      return false;
    }
  }

  class KalmanFilterExitCriterion implements Criterion {

    private double sdMultiplier;

    public void setSdMultiplier(double sdMultiplier) {
      this.sdMultiplier = sdMultiplier;
    }

    @Override
    public boolean isMet() throws CriterionViolationException {

      log.debug("Evaluating KalmanFilterExitCriterion criteria");
      try {
        if(tradingContext.getLastOrderBySymbol(secondSymbol).isLong() &&
            cointegration.getError() > sdMultiplier * sd ||
            tradingContext.getLastOrderBySymbol(secondSymbol).isShort() &&
            cointegration.getError() < -sdMultiplier * sd ) {
          log.info("error={}, sd={}", cointegration.getError(), sd);
          log.info("{} price {}; {} price {}", firstSymbol, tradingContext.getLastPrice(firstSymbol),
              secondSymbol, tradingContext.getLastPrice(secondSymbol));
          return true;
        }

      } catch (NoOrderAvailable noOrderAvailable) {
        log.debug("No orders available for " + secondSymbol);
        return false;
      } catch (PriceNotAvailableException e) {
        log.debug("No price available for some symbol");
        return false;
      }
      return false;
    }
  }
}
