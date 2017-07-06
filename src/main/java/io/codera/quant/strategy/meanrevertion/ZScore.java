package io.codera.quant.strategy.meanrevertion;

import com.google.common.util.concurrent.AtomicDouble;
import io.codera.quant.util.MathUtil;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import static com.google.common.base.Preconditions.checkArgument;

/**
 *
 */
public class ZScore {

  private double[] firstSymbolHistory;
  private double[] secondSymbolHistory;
  private int lookback;
  private int historyArraySize;
  private MathUtil u;

  private double[] x;
  private double[] y;
  private double[] yPort;

  private AtomicDouble lastCalculatedZScore;
  private AtomicDouble lastCalculatedHedgeRatio;

  private int historyIndex = 0;

  public ZScore(double[] firstSymbolHistory, double[] secondSymbolHistory, int lookback,
                MathUtil utils) {
    checkArgument(firstSymbolHistory.length == lookback * 2 - 1, "firstSymbolHistory should be of" +
        " " + (lookback * 2 - 1) + " size");
    checkArgument(secondSymbolHistory.length == lookback * 2 - 1, "secondHistory should be of " +
        (lookback * 2 - 1) + " size");

    this.firstSymbolHistory = firstSymbolHistory;
    this.secondSymbolHistory = secondSymbolHistory;
    this.lookback = lookback;
    this.u = utils;
  }

  public ZScore(int lookback,
                MathUtil utils) {
    this.lookback = lookback;
    this.historyArraySize = (lookback * 2 - 1);
    this.u = utils;
    firstSymbolHistory = new double[historyArraySize];
    secondSymbolHistory = new double[historyArraySize];
  }

  public double get(double firstSymbolPrice, double secondSymbolPrice) {
    checkArgument(firstSymbolPrice > 0, "firstSymbolPrice can not be <= 0");
    checkArgument(secondSymbolPrice > 0, "secondSymbolPrice can not be <= 0");

    if(firstSymbolHistory[firstSymbolHistory.length - 1] == 0 &&
        secondSymbolHistory[secondSymbolHistory.length - 1] == 0) {
      firstSymbolHistory[historyIndex] = firstSymbolPrice;
      secondSymbolHistory[historyIndex] = secondSymbolPrice;
      historyIndex++;
      return 0.0;
    }

    if(x == null && y == null && yPort == null) {
      x = new double[lookback];
      y = new double[lookback];
      yPort = new double[lookback];

      System.arraycopy(firstSymbolHistory, 0, x, 0, lookback - 1);
      System.arraycopy(secondSymbolHistory, 0, y, 0, lookback - 1);

      for(int i = lookback - 1; i < lookback * 2 - 1; i++) {
        x[lookback - 1] = firstSymbolHistory[i];
        y[lookback - 1] = secondSymbolHistory[i];

        RealMatrix xMatrix = MatrixUtils.createRealMatrix(lookback, 2);
        xMatrix.setColumn(0, x);
        xMatrix.setColumn(1, u.ones(lookback));

        OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression(0);
        ols.setNoIntercept(true);
        ols.newSampleData(y, xMatrix.getData());

        double hedgeRatio = ols.estimateRegressionParameters()[0];

        double yP = (-hedgeRatio * x[lookback - 1]) + y[lookback - 1];
        yPort[i + 1 - yPort.length] = yP;

        System.arraycopy(x, 1, x, 0, lookback - 1);
        System.arraycopy(y, 1, y, 0, lookback - 1);

      }

    }

    System.arraycopy(yPort, 1, yPort, 0, lookback - 1);
    x[lookback - 1] = firstSymbolPrice;
    y[lookback - 1] = secondSymbolPrice;

    RealMatrix xMatrix = MatrixUtils.createRealMatrix(lookback, 2);
    xMatrix.setColumn(0, x);
    xMatrix.setColumn(1, u.ones(lookback));

    OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression(0);
    ols.setNoIntercept(true);
    ols.newSampleData(y, xMatrix.getData());

    double hedgeRatio = ols.estimateRegressionParameters()[0];

    if(lastCalculatedHedgeRatio == null) {
      lastCalculatedHedgeRatio = new AtomicDouble();
    }

    lastCalculatedHedgeRatio.set(hedgeRatio);

    double yP = (-hedgeRatio * firstSymbolPrice) + secondSymbolPrice;
    yPort[lookback - 1] = yP;

    DescriptiveStatistics ds = new DescriptiveStatistics(yPort);

    double movingAverage = ds.getMean();
    double standardDeviation = ds.getStandardDeviation();

    System.arraycopy(x, 1, x, 0, lookback - 1);
    System.arraycopy(y, 1, y, 0, lookback - 1);

    double zScore = (yPort[lookback - 1] - movingAverage)/standardDeviation;

    if(lastCalculatedZScore == null) {
      lastCalculatedZScore = new AtomicDouble();
    }

    lastCalculatedZScore.set(zScore);
    return lastCalculatedZScore.get();
  }

  public double getHedgeRatio() {
    return lastCalculatedHedgeRatio.get();
  }

  public double getLastCalculatedZScore() {
    return lastCalculatedZScore.get();
  }

}
