package io.codera.quant.strategy;

import io.codera.quant.context.TradingContext;
import java.util.List;

/**
 *  Runs strategy in defined trading context.
 */
public interface StrategyRunner {

  /**
   * Run specified {@link Strategy} for symbols collection in given {@link TradingContext}.
   *
   * @param strategy strategy to run
   * @param symbols list
   */
  void run(Strategy strategy, List<String> symbols);

  /**
   * Stop the specified strategy for specified symbols.
   *
   * @param strategy strategy to stop
   * @param symbols symbols list
   */
  void stop(Strategy strategy, List<String> symbols);

}
