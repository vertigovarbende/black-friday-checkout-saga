package com.deveyk.orderflow.shared.messaging;

public final class EventDefinitions {

    // -------------- Order --------------
    public static final EventDefinition ORDER_CREATED = new EventDefinition(TopicNames.ORDER_CREATED, "OrderCreated", "order");
    public static final EventDefinition ORDER_COMPLETE_REQUESTED = new EventDefinition(TopicNames.ORDER_COMPLETE_REQUESTED, "OrderCompleteRequested", "order");
    public static final EventDefinition ORDER_COMPLETED = new EventDefinition(TopicNames.ORDER_COMPLETED, "OrderCompleted", "order");
    public static final EventDefinition ORDER_CANCEL_REQUESTED = new EventDefinition(TopicNames.ORDER_CANCEL_REQUESTED, "OrderCancelRequested", "order");
    public static final EventDefinition ORDER_CANCELLED = new EventDefinition(TopicNames.ORDER_CANCELLED, "OrderCancelled", "order");

    // -------------- Stock --------------
    public static final EventDefinition STOCK_RESERVE_REQUESTED = new EventDefinition(TopicNames.STOCK_RESERVE_REQUESTED, "StockReserveRequested", "stock");
    public static final EventDefinition STOCK_RESERVED = new EventDefinition(TopicNames.STOCK_RESERVED, "StockReserved", "stock");
    public static final EventDefinition STOCK_RESERVE_FAILED = new EventDefinition(TopicNames.STOCK_RESERVE_FAILED, "StockReserveFailed", "stock");
    public static final EventDefinition STOCK_RELEASE_REQUESTED = new EventDefinition(TopicNames.STOCK_RELEASE_REQUESTED, "StockReleaseRequested", "stock");
    public static final EventDefinition STOCK_RELEASED = new EventDefinition(TopicNames.STOCK_RELEASED, "StockReleased", "stock");

    // -------------- Payment --------------
    public static final EventDefinition PAYMENT_AUTHORIZE_REQUESTED = new EventDefinition(TopicNames.PAYMENT_AUTHORIZE_REQUESTED, "PaymentAuthorizeRequested", "payment");
    public static final EventDefinition PAYMENT_AUTHORIZED = new EventDefinition(TopicNames.PAYMENT_AUTHORIZED, "PaymentAuthorized", "payment");
    public static final EventDefinition PAYMENT_AUTHORIZE_FAILED = new EventDefinition(TopicNames.PAYMENT_AUTHORIZE_FAILED, "PaymentAuthorizeFailed", "payment");
    public static final EventDefinition PAYMENT_CAPTURE_REQUESTED = new EventDefinition(TopicNames.PAYMENT_CAPTURE_REQUESTED, "PaymentCaptureRequested", "payment");
    public static final EventDefinition PAYMENT_CAPTURED = new EventDefinition(TopicNames.PAYMENT_CAPTURED, "PaymentCaptured", "payment");
    public static final EventDefinition PAYMENT_VOID_REQUESTED = new EventDefinition(TopicNames.PAYMENT_VOID_REQUESTED, "PaymentVoidRequested", "payment");
    public static final EventDefinition PAYMENT_VOIDED = new EventDefinition(TopicNames.PAYMENT_VOIDED, "PaymentVoided", "payment");

    // -------------- Checkout (customer cancellation) --------------
    public static final EventDefinition CHECKOUT_CANCEL_REQUESTED = new EventDefinition(TopicNames.CHECKOUT_CANCEL_REQUESTED, "CheckoutCancelRequested", "checkout");
    public static final EventDefinition CHECKOUT_CANCEL_REJECTED = new EventDefinition(TopicNames.CHECKOUT_CANCEL_REJECTED, "CheckoutCancelRejected", "checkout");

    // -------------- Product --------------
    public static final EventDefinition PRODUCT_CHANGED = new EventDefinition(TopicNames.PRODUCT_CHANGED, "ProductChanged", "product");

    private EventDefinitions() {
    }


}
