package io.codera.quant.exception;

/**
 *  Thrown when price is not yet available
 */
public class PriceNotAvailableException extends Exception {

  public PriceNotAvailableException() {
    super();
  }

  public PriceNotAvailableException(String msg) {
     super(msg);
  }
}
