package org.lst.trading.lib.util.yahoo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import org.lst.trading.lib.csv.CsvReader;
import org.lst.trading.lib.series.DoubleSeries;
import org.lst.trading.lib.util.HistoricalPriceService;
import org.lst.trading.lib.util.Http;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import static java.lang.String.format;
import static java.nio.file.Files.lines;
import static java.nio.file.Paths.get;
import static org.lst.trading.lib.csv.CsvReader.ParseFunction.doubleColumn;
import static org.lst.trading.lib.csv.CsvReader.ParseFunction.ofColumn;

public class YahooFinance implements HistoricalPriceService {
  public static final String SEP = ",";
  public static final CsvReader.ParseFunction<Instant> DATE_COLUMN =
      ofColumn("Date")
          .map(s -> LocalDate.from(DateTimeFormatter.ISO_DATE.parse(s))
              .atStartOfDay(ZoneOffset.UTC.normalized())
              .toInstant());
  public static final CsvReader.ParseFunction<Double> CLOSE_COLUMN = doubleColumn("Close");
  public static final CsvReader.ParseFunction<Double> HIGH_COLUMN = doubleColumn("High");
  public static final CsvReader.ParseFunction<Double> LOW_COLUMN = doubleColumn("Low");
  public static final CsvReader.ParseFunction<Double> OPEN_COLUMN = doubleColumn("Open");
  public static final CsvReader.ParseFunction<Double> ADJ_COLUMN = doubleColumn("Adj Close");
  public static final CsvReader.ParseFunction<Double> VOLUME_COLUMN = doubleColumn("Volume");
  public static final OffsetDateTime DEFAULT_FROM =
      OffsetDateTime.of(2010, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
  private Connection connection = null;

  public YahooFinance(){}
  public YahooFinance(Connection connection) {
    this.connection = connection;
  }

  private static final Logger log = LoggerFactory.getLogger(YahooFinance.class);

  @Override public Observable<DoubleSeries> getHistoricalAdjustedPrices(String symbol) {
    return getHistoricalAdjustedPrices(symbol, DEFAULT_FROM.toInstant());
  }

  public Observable<DoubleSeries> getHistoricalAdjustedPrices(String symbol, Instant from) {
    return getHistoricalAdjustedPrices(symbol, from, Instant.now());
  }

  public Observable<DoubleSeries> getHistoricalAdjustedPrices(String symbol, Instant from, Instant to) {
    return getHistoricalPricesCsv(symbol, from, to).map(csv -> csvToDoubleSeries(csv, symbol));
  }

  private static Observable<String> getHistoricalPricesCsv(String symbol, Instant from, Instant to) {
    return Http.get(createHistoricalPricesUrl(symbol, from, to))
        .flatMap(Http.asString());
  }

  private static DoubleSeries csvToDoubleSeries(String csv, String symbol) {
    Stream<String> lines = Stream.of(csv.split("\n"));
    DoubleSeries prices = CsvReader.parse(lines, SEP, DATE_COLUMN, ADJ_COLUMN);
    prices.setName(symbol);
    prices = prices.toAscending();
    return prices;
  }

  public DoubleSeries readCsvToDoubleSeries(String csvFilePath, String symbol)
      throws IOException {
    Stream<String> lines = lines(get(csvFilePath));
    DoubleSeries prices = CsvReader.parse(lines, SEP, DATE_COLUMN, ADJ_COLUMN);
    prices.setName(symbol);
    prices = prices.toAscending();
    return prices;
  }

  public DoubleSeries readCsvToDoubleSeriesFromResource(String csvResourcePath, String symbol)
      throws IOException, URISyntaxException {
    URL resourceUrl = getResource(csvResourcePath);
    Stream<String> lines = lines(get(resourceUrl.toURI()));
    DoubleSeries prices = CsvReader.parse(lines, SEP, DATE_COLUMN, ADJ_COLUMN);
    prices.setName(symbol);
    prices = prices.toAscending();
    return prices;
  }

  public DoubleSeries readSeriesFromDb(String symbol) throws SQLException {
    if(connection == null) {
      return new DoubleSeries(symbol);
    }
    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM quotes WHERE symbol='%s'", symbol));
    DoubleSeries doubleSeries = new DoubleSeries(symbol);
    while(rs.next()) {
      doubleSeries.add(rs.getDouble(3), Instant.ofEpochMilli(rs.getTime(4).getTime()));
    }
    rs.close();
    stmt.close();
    return doubleSeries;
  }

  private static String createHistoricalPricesUrl(String symbol, Instant from, Instant to) {
    return format("https://ichart.yahoo.com/table.csv?s=%s&%s&%s&g=d&ignore=.csv", symbol,
        toYahooQueryDate(from, "abc"), toYahooQueryDate(to, "def"));
  }

  private static String toYahooQueryDate(Instant instant, String names) {
    OffsetDateTime time = instant.atOffset(ZoneOffset.UTC);
    String[] strings = names.split("");
    return format("%s=%d&%s=%d&%s=%d", strings[0], time.getMonthValue() - 1, strings[1], time.getDayOfMonth(), strings[2], time.getYear());
  }

  private URL getResource(String resource){
    URL url ;

    //Try with the Thread Context Loader.
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if(classLoader != null){
      url = classLoader.getResource(resource);
      if(url != null){
        return url;
      }
    }

    //Let's now try with the classloader that loaded this class.
    classLoader = System.class.getClassLoader();
    if(classLoader != null){
      url = classLoader.getResource(resource);
      if(url != null){
        return url;
      }
    }

    //Last ditch attempt. Get the resource from the classpath.
    return ClassLoader.getSystemResource(resource);
  }
}
