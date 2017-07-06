package io.codera.quant.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.ib.client.Controller;
import com.ib.controller.ApiController;
import com.ib.controller.OrderType;
import io.codera.quant.context.IbTradingContext;
import io.codera.quant.context.TradingContext;
import io.codera.quant.strategy.Criterion;
import io.codera.quant.strategy.IbPerMinuteStrategyRunner;
import io.codera.quant.strategy.Strategy;
import io.codera.quant.strategy.StrategyRunner;
import io.codera.quant.strategy.criterion.NoOpenOrdersExistEntryCriterion;
import io.codera.quant.strategy.criterion.OpenIbOrdersExistForAllSymbolsExitCriterion;
import io.codera.quant.strategy.criterion.common.NoPendingOrdersCommonCriterion;
import io.codera.quant.strategy.kalman.KalmanFilterStrategy;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.lst.trading.main.strategy.kalman.Cointegration;

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
  Controller apiController() {
    ApiController controller =
        new ApiController(new IbConnectionHandler(), valueOf -> {
        }, valueOf -> {});
    controller.connect(host, port, 0);
    return controller;
  }

  @Provides
  TradingContext tradingContext(Controller controller) throws SQLException, ClassNotFoundException {
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

    Strategy strategy = new KalmanFilterStrategy(
        contracts.get(0),
        contracts.get(1),
        tradingContext,
        new Cointegration(1e-4, 1e-3));

    Criterion noPendingOrdersCommonCriterion =
        new NoPendingOrdersCommonCriterion(tradingContext, contracts);

    Criterion noOpenOrdersExistCriterion =
        new NoOpenOrdersExistEntryCriterion(tradingContext, contracts);

    Criterion openOrdersExistForAllSymbolsCriterion =
        new OpenIbOrdersExistForAllSymbolsExitCriterion(tradingContext, contracts);

    ((KalmanFilterStrategy)strategy).setEntrySdMultiplier(1.2);
    ((KalmanFilterStrategy)strategy).setErrorQueueSize(30);
    ((KalmanFilterStrategy)strategy).setExitMultiplier(2);
//    Criterion stopLoss = new DefaultStopLossCriterion(contracts, -100, tradingContext);

    strategy.addCommonCriterion(noPendingOrdersCommonCriterion);

    strategy.addEntryCriterion(noOpenOrdersExistCriterion);

    strategy.addExitCriterion(openOrdersExistForAllSymbolsCriterion);
//    strategy.addStopLossCriterion(stopLoss);

    return strategy;

  }

}
