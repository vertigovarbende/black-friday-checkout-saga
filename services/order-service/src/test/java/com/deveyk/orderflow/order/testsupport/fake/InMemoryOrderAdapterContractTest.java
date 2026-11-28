package com.deveyk.orderflow.order.testsupport.fake;

import com.deveyk.orderflow.order.application.order.port.OrderReadPort;
import com.deveyk.orderflow.order.application.order.port.OrderSavePort;
import com.deveyk.orderflow.order.testsupport.contract.OrderPortContract;

class InMemoryOrderAdapterContractTest implements OrderPortContract {

    private final InMemoryOrderAdapter adapter = new InMemoryOrderAdapter();

    @Override
    public OrderReadPort readPort() {
        return adapter;
    }

    @Override
    public OrderSavePort savePort() {
        return adapter;
    }

    @Override
    public void inTransaction(final Runnable action) {
        action.run();
    }
}
