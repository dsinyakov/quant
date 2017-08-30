package io.codera.quant.observers;

import com.ib.client.OrderStatus;
import com.ib.controller.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public interface OrderObserver extends ApiController.IOrderHandler {

  Logger logger = LoggerFactory.getLogger(OrderObserver.class);

  default void orderStatus(OrderStatus status, double filled, double remaining, double avgFillPrice,
  long permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
    logger.info("Order status update: OrderStatus = {}, filled {}, remaining {}, avgFillPrice =" +
            " {}, permId = {}, parentId = {}, lastFillPrice = {}, clientId = {}, whyHeld = {}",
        status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId,
        whyHeld);
  }

  @Override
  default void handle(int errorCode, String errorMsg) {
     logger.error("errorCode = {}, errorMsg = {}", errorCode, errorMsg);
  }
}
