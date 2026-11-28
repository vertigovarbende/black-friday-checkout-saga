package com.deveyk.orderflow.shared.messaging;

public final class TopicNames {

    // -------------- Order --------------
    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_COMPLETE_REQUESTED = "order.complete.requested";
    public static final String ORDER_COMPLETED = "order.completed";
    public static final String ORDER_CANCEL_REQUESTED = "order.cancel.requested";
    public static final String ORDER_CANCELLED = "order.cancelled";

    // -------------- Stock --------------
    public static final String STOCK_RESERVE_REQUESTED = "stock.reserve.requested";
    public static final String STOCK_RESERVED = "stock.reserved";
    public static final String STOCK_RESERVE_FAILED = "stock.reserve.failed";
    public static final String STOCK_RELEASE_REQUESTED = "stock.release.requested";
    public static final String STOCK_RELEASED = "stock.released";

    // -------------- Payment --------------
    public static final String PAYMENT_AUTHORIZE_REQUESTED = "payment.authorize.requested";
    public static final String PAYMENT_AUTHORIZED = "payment.authorized";
    public static final String PAYMENT_AUTHORIZE_FAILED = "payment.authorize.failed";
    public static final String PAYMENT_CAPTURE_REQUESTED = "payment.capture.requested";
    public static final String PAYMENT_CAPTURED = "payment.captured";
    public static final String PAYMENT_VOID_REQUESTED = "payment.void.requested";
    public static final String PAYMENT_VOIDED = "payment.voided";

    // -------------- Checkout (customer cancellation) --------------
    public static final String CHECKOUT_CANCEL_REQUESTED = "checkout.cancel.requested";
    public static final String CHECKOUT_CANCEL_REJECTED = "checkout.cancel.rejected";

    // -------------- Product --------------
    public static final String PRODUCT_CHANGED = "product.changed";

    private TopicNames() {
    }

    public static String deadLetter(final String topic, final String groupId) {
        return topic + "." + groupId + ".DLT";
    }

}
