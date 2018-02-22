package com.somebank.matcher.pojos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Order {
    private final OrderDirection orderDirection;
    private final RIC ric;
    private final long quantity;
    private final BigDecimal price;
    private final User user;


    public static final String INVALUD_ARG_MSG = "Arguments for an Order must not be null.";

    private static final int ROUNDING_SCALE = 4;

    public Order(OrderDirection orderDirection, RIC ric, Long quantity, BigDecimal price, User user) {

        this.orderDirection = Objects.requireNonNull(orderDirection, INVALUD_ARG_MSG);
        this.ric = Objects.requireNonNull(ric, INVALUD_ARG_MSG);
        this.quantity = Objects.requireNonNull(quantity, INVALUD_ARG_MSG);
        this.price = Objects.requireNonNull(price.setScale(ROUNDING_SCALE, RoundingMode.HALF_DOWN), INVALUD_ARG_MSG);
        this.user = Objects.requireNonNull(user, INVALUD_ARG_MSG);
    }

    public OrderDirection getOrderDirection() {
        return orderDirection;
    }

    public RIC getRic() {
        return ric;
    }

    public long getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public User getUser() {
        return user;
    }
}
