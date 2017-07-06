package io.codera.quant.observers;

import com.ib.controller.NewOrderState;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 *
 */
public class IbOrderObserver implements OrderObserver {

  private final PublishSubject<NewOrderState> orderSubject;

  public IbOrderObserver() {
    orderSubject = PublishSubject.create();
  }

  @Override
  public void orderState(NewOrderState orderState) {
    orderSubject.onNext(orderState);
  }

  public Observable<NewOrderState> observableOrderState() {
    return orderSubject.asObservable();
  }
}
