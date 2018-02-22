package com.somebank.matcher;

import com.somebank.matcher.domain.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class OrderReporting {

    private static final int DEC_SCALE = 4;
    private static final String INVALID_ARG_MSG = "Argument must not be null.";


    public static Map<BigDecimal, Long> getOpenInterest(RIC ric, OrderDirection orderDirection, Queue<Order> openOrders) {
        Objects.requireNonNull(ric, INVALID_ARG_MSG);
        Objects.requireNonNull(orderDirection, INVALID_ARG_MSG);
        Objects.requireNonNull(openOrders, INVALID_ARG_MSG);

        if (openOrders.isEmpty()) {
            return Collections.emptyMap();
        }

        return openOrders.stream()
                .filter(o -> o.getRic().equals(ric))
                .filter(o -> o.getOrderDirection() == orderDirection)
                .collect(Collectors.groupingBy(Order::getPrice, Collectors.summingLong(Order::getQuantity)));
    }

    public static BigDecimal getAveragePerUnitExecutionPrice(RIC ric, Queue<ExecutedOrder> executedOrders) {
        Objects.requireNonNull(ric, INVALID_ARG_MSG);
        Objects.requireNonNull(executedOrders, INVALID_ARG_MSG);

        if (executedOrders.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<ExecutedOrder> matches = executedOrders.stream()
                .filter(o -> o.getRic().equals(ric))
                .collect(Collectors.toList());

        if (matches.size() == 0) {
            return BigDecimal.ZERO;
        }

        long totalQuantity = 0;
        BigDecimal perUnitAvg = BigDecimal.ZERO;
        for (ExecutedOrder order : matches) {
            totalQuantity += order.getQuantity();
            perUnitAvg = perUnitAvg.add(order.getExecutedPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
        }

        return perUnitAvg.divide(new BigDecimal(totalQuantity), DEC_SCALE, RoundingMode.HALF_DOWN);
    }

    public static long getExecutedQuantity(RIC ric, User user, Queue<ExecutedOrder> executedOrders) {

        Objects.requireNonNull(ric, INVALID_ARG_MSG);
        Objects.requireNonNull(user, INVALID_ARG_MSG);
        Objects.requireNonNull(executedOrders, INVALID_ARG_MSG);

        if (executedOrders.isEmpty()) {
            return 0;
        }

        List<Order> userOrders = executedOrders.stream()
                .filter(o -> o.getRic().equals(ric))
                .flatMap(o -> Arrays.stream(o.getExecutedTrade()))
                .filter(o -> o.getUser().equals(user))
                .collect(Collectors.toList());

        long balance = 0;
        for (Order order : userOrders) {
            if (order.getOrderDirection() == OrderDirection.SELL) {
                balance -= order.getQuantity();
            } else {
                balance += order.getQuantity();
            }
        }
        return balance;
    }
}
