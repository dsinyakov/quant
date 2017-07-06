package org.lst.trading.main;

public class BacktestMain {
//  public static void main(String[] args) throws URISyntaxException, IOException {
//    String x = "EWA";
//    String y = "EWC";
//
//    // initialize the trading strategy
//    TradingStrategy strategy = new CointegrationTradingStrategy(x, y);
//
//    DoubleSeries ewa = new DoubleSeries("EWA");
//    DoubleSeries ewc = new DoubleSeries("EWC");
//
//    AtomicInteger i = new AtomicInteger();
//    i.set(0);
//
//    try (Stream<String> stream =
//             lines(get("/Users/beastie/Downloads/EWA.csv"))) {
//
//      stream.forEachOrdered(s -> ewa.add(Double.parseDouble(s), Instant.ofEpochSecond(i.getAndAdd
//          (86400))));
//
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//
//    i.set(0);
//    try (Stream<String> stream =
//             lines(get("/Users/beastie/Downloads/EWC.csv"))) {
//
//      stream.forEachOrdered(s -> ewc.add(Double.parseDouble(s), Instant.ofEpochSecond(i.getAndAdd
//          (86400))));
//
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//
//    MultipleDoubleSeries priceSeries = new MultipleDoubleSeries(ewa, ewc);
//
//    // initialize the backtesting engine
//    int deposit = 15000;
//    BackTest backTest = new BackTest(deposit, priceSeries);
//    backTest.setLeverage(4);
//
//
//    // do the backtest
////    BackTest.Result result = backTest.run(strategy);
//
//    // show results
//    StringBuilder orders = new StringBuilder();
//    orders.append("id,amount,side,instrument,from,to,open,close,pl\n");
//    for (ClosedOrder order : result.getOrders()) {
//      orders.append(format(Locale.US, "%d,%d,%s,%s,%s,%s,%f,%f,%f\n", order.getId(), Math.abs(order.getAmount()), order.isLong() ? "Buy" : "Sell", order.getInstrument(), order.getOpenInstant(), order.getCloseInstant(), order.getOpenPrice(), order.getClosePrice(), order.getPl()));
//    }
//    System.out.print(orders);
//
//    int days = priceSeries.size();
//
//    System.out.println();
//    System.out.println("Backtest result of " + strategy.getClass() + ": " + strategy);
//    System.out.println("Prices: " + priceSeries);
//    System.out.println(format(Locale.US, "Simulated %d days, Initial deposit %d, Leverage %f", days, deposit, backTest.getLeverage()));
//    System.out.println(format(Locale.US, "Commissions = %f", result.getCommissions()));
//    System.out.println(format(Locale.US, "P/L = %.2f, Final value = %.2f, Result = %.2f%%, Annualized = %.2f%%, Sharpe (rf=0%%) = %.2f", result.getPl(), result.getFinalValue(), result.getReturn() * 100, result.getReturn() / (days / 251.) * 100, result.getSharpe()));
//
//    System.out.println("Orders: " + Util.writeStringToTempFile(orders.toString()));
//    System.out.println("Statistics: " + Util.writeCsv(new MultipleDoubleSeries(result.getPlHistory(), result.getMarginHistory())));
//  }
}
