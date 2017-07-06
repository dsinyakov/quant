package com.ib.client;

import com.ib.controller.ApiController;
import com.ib.controller.NewContract;

/**
 * Interface for requesting data from IB
 */
public interface DataRequestor {

  void reqTopMktData(NewContract contract, String genericTickList, boolean snapshot,
                     ApiController.ITopMktDataHandler handler);

  void cancelTopMktData(ApiController.ITopMktDataHandler handler);
}
