package com.ib.client;

import com.ib.controller.ApiController.IAccountHandler;

/**
 *
 */
public interface AccountUpdater {

  void reqAccountUpdates(boolean subscribe, String acctCode, IAccountHandler handler);
}
