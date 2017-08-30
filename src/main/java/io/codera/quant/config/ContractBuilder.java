package io.codera.quant.config;

import com.google.common.collect.ImmutableMap;
import com.ib.client.Contract;
import com.ib.client.Types;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Builds the contract
 */
public class ContractBuilder {

  private final static Logger log = LoggerFactory.getLogger(ContractBuilder.class);
  private static final Map<String, Integer> futuresMap = ImmutableMap.of("ES=F", 50, "YM=F", 5,
      "TF=F", 50);

  public Contract build(String symbolName) {
    Contract contract = new Contract();

    if(symbolName.contains("/")) {
      log.debug("{} is a Forex symbol", symbolName);

      String[] fxSymbols = symbolName.split("/");

      contract.symbol(fxSymbols[0]);
      contract.exchange("IDEALPRO");
      contract.secType(Types.SecType.CASH);
      contract.currency(fxSymbols[1]);
      return contract;
    } else if(symbolName.contains("=F")){
      String s = symbolName.replace("=F", "");
      Map<String, String> futuresMap = ImmutableMap.of(
          "ES", "GLOBEX",
          "YM", "ECBOT",
          "TF", "NYBOT");
      contract.symbol(s);
      contract.exchange(futuresMap.get(s));
//      contract.expiry("201706");
      contract.secType(Types.SecType.FUT);
      contract.currency("USD");
      log.info("Contract " + contract);
      return contract;
    }
    contract.symbol(symbolName);
    contract.localSymbol(symbolName);
    contract.exchange("SMART");
    contract.primaryExch("ARCA");
    contract.secType(Types.SecType.STK);
    contract.currency("USD");
    return contract;
  }

  /**
   * If its a forex symbol, then for some strategies it is necessary to flip the price to
   * understand the price based on USD (e.g. how much dollars is needed to buy 1 unit of currency
   * in traded pair)
   *
   * @param symbol symbol name
   * @return adjusted price, basically 1/<pair price>
   */
  public static double getSymbolPrice(String symbol, double price) {
    if(symbol.contains("/")) {
      String[] fxSymbols = symbol.split("/");
      if(fxSymbols[0].equals("USD")) {
        return 1/price;
      }
    }
    return price;
  }

  public static Integer getFutureMultiplier(String futureSymbol) {
    checkArgument(futureSymbol != null, "symbol is null");
    // TODO (Dsinyakov) : refactor to throw exception instead of returning null
    return futuresMap.get(futureSymbol);
  }
}
