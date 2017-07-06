package com.ib.client;

/**
 * Interface for {@link com.ib.controller.ApiController}
 */
public interface Controller extends DataRequestor, OrderSubmitter, AccountUpdater, HistoryRequestor {
}
