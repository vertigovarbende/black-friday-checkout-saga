package com.deveyk.orderflow.shared.messaging.contract;

public record OrderflowEvent<P>(EventMetadata metadata, P payload) {


}
