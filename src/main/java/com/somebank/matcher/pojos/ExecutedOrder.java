package com.somebank.matcher.pojos;

import com.somebank.matcher.pojos.Order;
import com.somebank.matcher.pojos.RIC;

import java.math.BigDecimal;
import java.util.Objects;

public final class ExecutedOrder {
    private final Order[] matched;
    private final RIC ric;
    private final long quantity;
    private final BigDecimal executedPrice;

    public ExecutedOrder(Order[] matched, BigDecimal executedPrice) {
        this.matched = Objects.requireNonNull(matched);
        this.executedPrice = Objects.requireNonNull(executedPrice);

        this.quantity = matched[0].getQuantity();
        this.ric = matched[0].getRic();
    }

    public Order[] getExecutedTrade() {
        return matched;
    }

    public BigDecimal getExecutedPrice() {
        return executedPrice;
    }


    public long getQuantity() {
        return quantity;
    }

    public RIC getRic() {
        return ric;
    }
}
