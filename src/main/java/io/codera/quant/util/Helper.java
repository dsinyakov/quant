package io.codera.quant.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.ib.client.Controller;
import com.ib.controller.ApiController;
import com.ib.controller.NewContract;
import com.ib.controller.Types;
import io.codera.quant.config.ContractBuilder;
import io.codera.quant.config.IbConnectionHandler;
import io.codera.quant.observers.HistoryObserver;
import io.codera.quant.observers.IbHistoryObserver;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDate;
import org.lst.trading.lib.series.DoubleSeries;
import org.lst.trading.lib.series.MultipleDoubleSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 *
 */
public class Helper {

  private static Logger logger = LoggerFactory.getLogger(Helper.class);

  public static void getFxQuotes(String baseSymbol, String symbol) throws IOException,
      NoSuchFieldException, IllegalAccessException {
    DateTime dt1 = new DateTime(2012, 3, 26, 12, 0, 0, 0);
    DateTime dt2 = new DateTime(2017, 1, 1, 12, 0, 0, 0);
    LocalDate startDate = new LocalDate(dt1);
    LocalDate endDate = new LocalDate(dt2);

    RestTemplate restOperations = new RestTemplate();

    List<HttpMessageConverter<?>> converters = restOperations.getMessageConverters();
    for (HttpMessageConverter<?> converter : converters) {
      if (converter instanceof MappingJackson2HttpMessageConverter) {
        MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
        jsonConverter.setObjectMapper(new ObjectMapper());
        jsonConverter.setSupportedMediaTypes(
            ImmutableList.of(new MediaType("application", "json", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET),
                new MediaType("text", "javascript", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET)));
      }
    }

    int days = Days.daysBetween(startDate, endDate).getDays();

    List<String> lines = Lists.newLinkedList();
    lines.add("Date, Adj Close");
    Path file = Paths.get(String.format("/Users/beastie/Downloads/%s%s_quotes.csv",
        baseSymbol.toLowerCase(),
        symbol.toLowerCase()));
    for (int i = 0; i < days; i++) {
      LocalDate d = startDate.withFieldAdded(DurationFieldType.days(), i);

      Response res =
          restOperations.getForObject(String.format("http://api.fixer.io/%s?base=%s&symbols=%s",
              d, baseSymbol, symbol),
              Response.class);
      Field field = Response.Rates.class.getField(symbol.toLowerCase());
      double resSymbol = (double) field.get(res.rates);
      lines.add(String.format("%s, %s", d, resSymbol));
      logger.info("{} {}/{} -  {}", res.date, baseSymbol, symbol, resSymbol);

    }
    Files.write(file, lines, Charset.forName("UTF-8"));

  }

  public static MultipleDoubleSeries getHistoryForSymbols(
      Controller controller,
      int daysOfHistory,
      List<String> symbols
  ) {

    DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
    String date = LocalDateTime.now().format(formatter);

    ContractBuilder contractBuilder = new ContractBuilder();

    List<DoubleSeries> doubleSeries = Lists.newArrayList();
    for(String symbol : symbols) {
      NewContract contract = contractBuilder.build(symbol);
      HistoryObserver historyObserver = new IbHistoryObserver(symbol);
      controller.reqHistoricalData(contract, date, daysOfHistory, Types.DurationUnit.DAY,
          Types.BarSize._1_min, Types.WhatToShow.TRADES, false, historyObserver);
      doubleSeries.add(((IbHistoryObserver)historyObserver).observableDoubleSeries()
          .toBlocking()
          .first());

    }

    return new MultipleDoubleSeries(doubleSeries);
  }


  static class Response {
    private String base;
    private String date;
    private Rates rates;

    public Rates getRates() {
      return rates;
    }

    public void setRates(Rates rates) {
      this.rates = rates;
    }

    public String getBase() {
      return base;
    }

    public void setBase(String base) {
      this.base = base;
    }

    public String getDate() {
      return date;
    }

    public void setDate(String date) {
      this.date = date;
    }

    static class Rates {

      @JsonProperty("AUD")
      public double aud;

      @JsonProperty("CAD")
      public double cad;

      @JsonProperty("CHF")
      public double chf;

      @JsonProperty("CYP")
      public double cyp;

      @JsonProperty("CZK")
      public double czk;

      @JsonProperty("DKK")
      public double dkk;

      @JsonProperty("EEK")
      public double eek;

      @JsonProperty("GBP")
      public double gbp;

      @JsonProperty("HKD")
      public double hkd;

      @JsonProperty("HUF")
      public double huf;

      @JsonProperty("ISK")
      public double isk;

      @JsonProperty("JPY")
      public double jpy;

      @JsonProperty("KRW")
      public double krw;

      @JsonProperty("LTL")
      public double ltl;

      @JsonProperty("LVL")
      public double lvl;

      @JsonProperty("MTL")
      public double mtl;

      @JsonProperty("NOK")
      public double nok;

      @JsonProperty("NZD")
      public double nzd;

      @JsonProperty("PLN")
      public double pln;

      @JsonProperty("ROL")
      public double rol;

      @JsonProperty("SEK")
      public double sek;

      @JsonProperty("SGD")
      public double sgd;

      @JsonProperty("SIT")
      public double sit;

      @JsonProperty("SKK")
      public double skk;

      @JsonProperty("TRY")
      public double trl;

      @JsonProperty("ZAR")
      public double zar;

      @JsonProperty("EUR")
      public double eur;

      @JsonProperty("BGN")
      public double bgn;

      @JsonProperty("BRL")
      public double brl;

      @JsonProperty("CNY")
      public double cny;

      @JsonProperty("HRK")
      public double hrk;

      @JsonProperty("IDR")
      public double idr;

      @JsonProperty("ILS")
      public double ils;

      @JsonProperty("INR")
      public double inr;

      @JsonProperty("MXN")
      public double mxn;

      @JsonProperty("MYR")
      public double myr;

      @JsonProperty("PHP")
      public double php;

      @JsonProperty("RON")
      public double ron;

      @JsonProperty("RUB")
      public double rub;

      @JsonProperty("THB")
      public double thb;

      @JsonProperty("USD")
      public double usd;

      @Override
      public String toString() {
        return
            "aud=" + aud +
                ", cad=" + cad +
                ", chf=" + chf +
                ", cyp=" + cyp +
                ", czk=" + czk +
                ", dkk=" + dkk +
                ", eek=" + eek +
                ", gbp=" + gbp +
                ", hkd=" + hkd +
                ", huf=" + huf +
                ", isk=" + isk +
                ", jpy=" + jpy +
                ", krw=" + krw +
                ", ltl=" + ltl +
                ", lvl=" + lvl +
                ", mtl=" + mtl +
                ", nok=" + nok +
                ", nzd=" + nzd +
                ", pln=" + pln +
                ", rol=" + rol +
                ", sek=" + sek +
                ", sgd=" + sgd +
                ", sit=" + sit +
                ", skk=" + skk +
                ", trl=" + trl +
                ", zar=" + zar +
                ", eur=" + eur +
                ", bgn=" + bgn +
                ", brl=" + brl +
                ", cny=" + cny +
                ", hrk=" + hrk +
                ", idr=" + idr +
                ", ils=" + ils +
                ", inr=" + inr +
                ", mxn=" + mxn +
                ", myr=" + myr +
                ", php=" + php +
                ", ron=" + ron +
                ", rub=" + rub +
                ", thb=" + thb +
                ", usd=" + usd;
      }
    }
  }


  public static double getMean(double[] data) {
    double sum = 0.0;
    for(double a : data)
      sum += a;
    return sum/data.length;
  }

  private static double getVariance(double[] data) {
    double mean = getMean(data);
    double temp = 0;
    for(double a :data)
      temp += (a-mean)*(a-mean);
    return temp/data.length;
  }

  public static double getStdDev(double[] data) {
    return Math.sqrt(getVariance(data));
  }
}
