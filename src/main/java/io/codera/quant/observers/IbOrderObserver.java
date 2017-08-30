package io.codera.quant.observers;

import com.ib.client.OrderState;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 *
 */
public class IbOrderObserver implements OrderObserver {

  private final PublishSubject<OrderState> orderSubject;

  public IbOrderObserver() {
    orderSubject = PublishSubject.create();
  }

  @Override
  public void orderState(OrderState orderState) {
    orderSubject.onNext(orderState);
  }

  public Observable<OrderState> observableOrderState() {
    return orderSubject.asObservable();
  }
}
