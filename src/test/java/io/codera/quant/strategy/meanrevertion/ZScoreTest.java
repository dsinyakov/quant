package io.codera.quant.strategy.meanrevertion;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import io.codera.quant.config.GuiceJUnit4Runner;
import io.codera.quant.util.MathUtil;
import io.codera.quant.context.TradingContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lst.trading.lib.series.DoubleSeries;
import org.lst.trading.lib.util.yahoo.YahooFinance;

import static org.junit.Assert.assertEquals;


/**
 * Tests for {@link ZScore}
 */
@RunWith(GuiceJUnit4Runner.class)
public class ZScoreTest {
  @Inject
  private TradingContext tradingContext;
  private static final List<String> SYMBOLS = ImmutableList.of("GLD", "USO");
  private static final int LOOKBACK = 20;
  private static final int MINUTES_OF_HISTORY = LOOKBACK * 2 - 1;

  // This test requires a working IB TWS.
  // Test returns inconsistent result, due to probably a bug in historical data retrieval timing
  @Test
  public void getTest() throws Exception {
    for(String symbol : SYMBOLS) {
      tradingContext.addContract(symbol);
    }
    DoubleSeries firstSymbolHistory =
        tradingContext.getHistoryInMinutes(SYMBOLS.get(0), MINUTES_OF_HISTORY);
    DoubleSeries secondSymbolHistory =
        tradingContext.getHistoryInMinutes(SYMBOLS.get(1), MINUTES_OF_HISTORY);

    ZScore zScore =
        new ZScore(firstSymbolHistory.toArray(), secondSymbolHistory.toArray(), LOOKBACK, new
            MathUtil());
    System.out.println(zScore.get(114.7, 10.30));
  }

  @Test
  public void getTestUnit() throws IOException, URISyntaxException {
    YahooFinance finance = new YahooFinance();
    DoubleSeries gld =
        finance.readCsvToDoubleSeriesFromResource("GLD.csv", SYMBOLS.get(0));

    DoubleSeries uso =
        finance.readCsvToDoubleSeriesFromResource("USO.csv", SYMBOLS.get(1));
    ZScore zScore = new ZScore(gld.toArray(), uso.toArray(), LOOKBACK, new MathUtil());
    assertEquals("Failed", -1.0102216127916113, zScore.get(58.33, 66.35), 0);
    assertEquals("Failed", -0.9692409006953596, zScore.get(57.73, 67), 0);
    assertEquals("Failed", -0.9618287583543594, zScore.get(57.99, 66.89), 0);
  }

}