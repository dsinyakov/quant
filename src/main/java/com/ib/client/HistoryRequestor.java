package com.ib.client;

import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.NewContract;
import com.ib.controller.Types;

/**
 *
 */
public interface HistoryRequestor {

  void reqHistoricalData(
      NewContract contract,
      String endDateTime,
      int duration,
      Types.DurationUnit durationUnit,
      Types.BarSize barSize,
      Types.WhatToShow whatToShow,
      boolean rthOnly,
      IHistoricalDataHandler handler);
}
