package com.ib.client;

import com.ib.controller.ApiController.IOrderHandler;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;

/**
 * Adding missing interfaces to simplify development/testing
 */
public interface OrderSubmitter {

  void placeOrModifyOrder(NewContract contract, NewOrder order, IOrderHandler handler);
}
