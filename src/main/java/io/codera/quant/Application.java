package io.codera.quant;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.codera.quant.config.Config;
import io.codera.quant.strategy.Strategy;
import io.codera.quant.strategy.StrategyRunner;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkState;

/**
 * Entry point for PT trading app.
 */
public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    Options options = new Options();

    options.addOption("h", true, "Interactive Brokers host");
    options.addOption("p", true, "Interactive Brokers port");
    options.addOption("l", true, "List of symbols to trade");

    // create the parser
    CommandLineParser parser = new DefaultParser();
    try {
      // parse the command line arguments
      CommandLine cmd = parser.parse(options, args);

      logger.info("Starting app");

      checkState(cmd.getOptionValue("h") != null, "host can not be null");
      checkState(cmd.getOptionValue("p") != null, "port can not be null");
      checkState(cmd.getOptionValue("l") != null, "symbol can not be null");

      Injector injector = Guice.createInjector(
          new Config(
              cmd.getOptionValue("h"),
              Integer.valueOf(cmd.getOptionValue("p")),
              cmd.getOptionValue("l")
          ));

      StrategyRunner strategyRunner = injector.getInstance(StrategyRunner.class);
      Strategy strategy = injector.getInstance(Strategy.class);
      String[] symbolList = cmd.getOptionValue("l").split(",");

      strategyRunner.run(strategy, Arrays.asList(symbolList));

    }
    catch(Exception e) {
      // oops, something went wrong
      logger.error("Something went wrong", e);
    }

  }
}
