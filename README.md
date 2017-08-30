# Codera Quant Java framework
Codera Quant Java framework allows development of automated algorithmic trading strategies, supports backtesting using historical data taken from Interactive Brokers, Yahoo Finance, local database or CSV files and
paper or live trade execution via Interactive Brokers TWS Java API.

# Prerequisites
1. Maven installed
2. TWS has to be up and running and listening on port 7497
3. Download TWS jar http://interactivebrokers.github.io/. Downloaded archive usually contains built jar file in IBJts/source/JavaClient dir (as of 9.72.17 version)
4. Install jar locally
`mvn install:install-file -DgroupId=tws-api -DartifactId=tws-api -Dversion=9.72.17-SNAPSHOT -Dfile=TwsApi.jar`
5. Add/update maven dependency e.g
```
<dependency>
  <groupId>tws-api</groupId>
  <artifactId>tws-api</artifactId>
  <version>9.72.17-SNAPSHOT</version>
</dependency>
```

# Strategy execution
`$ mvn exec:java@app -Dexec.args="-h localhost -p 7497 -l "SPY,IWM""`

# Backtest execution
`mvn exec:java@test`

