package org.lst.trading.lib.model;

import com.ib.client.OrderStatus;
import io.codera.quant.config.ContractBuilder;
import java.time.Instant;

public interface Order {
  int getId();

  int getAmount();

  double getOpenPrice();

  Instant getOpenInstant();

  String getInstrument();

  default OrderStatus getOrderStatus(){
    return OrderStatus.Inactive;
  }

  default boolean isLong() {
    return getAmount() > 0;
  }

  default boolean isShort() {
    return !isLong();
  }

  default int getSign() {
    return isLong() ? 1 : -1;
  }

  default double calculatePl(double currentPrice) {

    if(getInstrument().contains("=F")) {

      return getAmount() * (currentPrice - getOpenPrice()) *
          ContractBuilder.getFutureMultiplier(getInstrument());
    }
    return getAmount() * (currentPrice - getOpenPrice());
  }
}
