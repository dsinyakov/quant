package io.codera.quant.config;

import com.google.inject.Guice;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class GuiceJUnit4Runner extends BlockJUnit4ClassRunner {

  public GuiceJUnit4Runner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  public Object createTest() throws Exception {
    Object object = super.createTest();
    Guice.createInjector(new TestConfig()).injectMembers(object);
    return object;
  }

}