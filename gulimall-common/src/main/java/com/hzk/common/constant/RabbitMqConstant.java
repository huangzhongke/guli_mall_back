package com.hzk.common.constant;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/17 16:02
 */
public class RabbitMqConstant {

    public static final String ORDER_RELEASE_ORDER_QUEUE="order.release.order.queue";
    public static final String ORDER_DELAY_QUEUE="order.delay.queue";
    public static final String ORDER_EVENT_EXCHANGE="order-event-exchange";
    public static final String ORDER_CREATE_ORDER_ROUTING_KEY="order.create.order";
    public static final String ORDER_RELEASE_ORDER_ROUTING_KEY="order.release.order";
    public static final String ORDER_RELEASE_OTHER_ROUTING_KEY="order.release.other.#";

    public static final String STOCK_RELEASE_STOCK_QUEUE="stock.release.stock.queue";
    public static final String STOCK_DELAY_QUEUE="stock.delay.queue";
    public static final String STOCK_EVENT_EXCHANGE="stock-event-exchange";
    public static final String STOCK_LOCKED_ROUTING_KEY="stock.locked";
    public static final String STOCK_RELEASE_ORDER_ROUTING_KEY="stock.release.#";

}
