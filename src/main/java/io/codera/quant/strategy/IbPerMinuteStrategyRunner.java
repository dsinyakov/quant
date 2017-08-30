package io.codera.quant.strategy;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.joda.time.DateTime;

/**
 * Strategy runner that executes every minute
 */
public class IbPerMinuteStrategyRunner implements StrategyRunner {

  @Override
  public void run(Strategy strategy, List<String> symbols) {

    for(String symbol : symbols) {
      strategy.addSymbol(symbol);
    }

    Timer timer = new Timer(true);
    DateTime dt = new DateTime();

    timer.schedule(
        new TriggerTick(strategy),
        new Date((dt.getMillis() - (dt.getSecondOfMinute() * 1000)) + 59000),
        60000);

  }

  @Override
  public void stop(Strategy strategy, List<String> symbols) {

  }

  private class TriggerTick extends TimerTask {

    private final Strategy strategy;

    TriggerTick(Strategy strategy) {
      this.strategy = strategy;
    }

    @Override
    public void run() {
      strategy.onTick();
    }

  }
}
