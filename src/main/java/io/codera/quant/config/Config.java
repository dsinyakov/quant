package io.codera.quant.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.ib.client.OrderType;
import com.ib.controller.ApiController;
import io.codera.quant.context.IbTradingContext;
import io.codera.quant.context.TradingContext;
import io.codera.quant.strategy.Criterion;
import io.codera.quant.strategy.IbPerMinuteStrategyRunner;
import io.codera.quant.strategy.Strategy;
import io.codera.quant.strategy.StrategyRunner;
import io.codera.quant.strategy.criterion.NoOpenOrdersExistEntryCriterion;
import io.codera.quant.strategy.criterion.OpenIbOrdersExistForAllSymbolsExitCriterion;
import io.codera.quant.strategy.criterion.common.NoPendingOrdersCommonCriterion;
import io.codera.quant.strategy.criterion.stoploss.DefaultStopLossCriterion;
import io.codera.quant.strategy.meanrevertion.BollingerBandsStrategy;
import io.codera.quant.strategy.meanrevertion.ZScore;
import io.codera.quant.strategy.meanrevertion.ZScoreEntryCriterion;
import io.codera.quant.strategy.meanrevertion.ZScoreExitCriterion;
import io.codera.quant.util.MathUtil;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 */
public class Config extends AbstractModule {

  private final String host;
  private final int port;
  private final String symbolList;

  public Config(String host, int port, String symbolList) {
    this.host = host;
    this.port = port;
    this.symbolList = symbolList;
  }

  @Override
  protected void configure() {
    bind(StrategyRunner.class).to(IbPerMinuteStrategyRunner.class);
  }

  @Provides
  ApiController apiController() {
    ApiController controller =
        new ApiController(new IbConnectionHandler(), valueOf -> {
        }, valueOf -> {});
    controller.connect(host, port, 0, null);
    return controller;
  }

  @Provides
  TradingContext tradingContext(ApiController controller) throws SQLException, ClassNotFoundException {
    return new IbTradingContext(
        controller,
        new ContractBuilder(),
        OrderType.MKT,
//        DriverManager.getConnection("jdbc:mysql://localhost/fx", "root", "admin"),
        2
    );
  }

  @Provides
  Strategy strategy(TradingContext tradingContext) {
    List<String> contracts = Arrays.asList(symbolList.split(","));

    ZScore zScore = new ZScore(20, new MathUtil());

    Strategy strategy = new BollingerBandsStrategy(
        contracts.get(0),
        contracts.get(1),
        tradingContext,
        zScore);

    Criterion zScoreEntryCriterion = new ZScoreEntryCriterion(contracts.get(0), contracts.get(1), 1, zScore,
        tradingContext);

    Criterion zScoreExitCriterion = new ZScoreExitCriterion(contracts.get(0), contracts.get(1), 0, zScore,
        tradingContext);

    Criterion noPendingOrdersCommonCriterion =
        new NoPendingOrdersCommonCriterion(tradingContext, contracts);

    Criterion noOpenOrdersExistCriterion =
        new NoOpenOrdersExistEntryCriterion(tradingContext, contracts);

    Criterion openOrdersExistForAllSymbolsCriterion =
        new OpenIbOrdersExistForAllSymbolsExitCriterion(tradingContext, contracts);

    Criterion stopLoss = new DefaultStopLossCriterion(contracts, -100, tradingContext);

    strategy.addCommonCriterion(noPendingOrdersCommonCriterion);

    strategy.addEntryCriterion(noOpenOrdersExistCriterion);
    strategy.addEntryCriterion(zScoreEntryCriterion);

    strategy.addExitCriterion(openOrdersExistForAllSymbolsCriterion);
    strategy.addEntryCriterion(zScoreExitCriterion);

    strategy.addStopLossCriterion(stopLoss);

    return strategy;

  }

}
