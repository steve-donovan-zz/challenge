package com.somebank.matcher;

import com.somebank.matcher.domain.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class MatchingEngine {

    private static final String INVALID_ARG_MSG = "Argument must not be null.";

    private final Queue<Order> openOrders = new ConcurrentLinkedQueue();
    private final Queue<ExecutedOrder> executedOrders = new ConcurrentLinkedQueue();


    public void addOrder(Order order) {

        Objects.requireNonNull(order, INVALID_ARG_MSG);

        List<Order> matches = getOrderMatches(order);

        if (matches.size() > 0) {
            processMatchedOrders(order, matches);
        } else {
            openOrders.add(order);
        }
    }

    private void processMatchedOrders(Order latestOrder, List<Order> matches) {

        Objects.requireNonNull(latestOrder, INVALID_ARG_MSG);
        Objects.requireNonNull(matches, INVALID_ARG_MSG);

        Order[] orderPair = new Order[2];
        Order matchedOrder;

        if (matches.size() == 1) {
            matchedOrder = matches.get(0);
        } else {
            boolean isSamePrice = matches.stream()
                    .allMatch(o -> o.getPrice().compareTo(latestOrder.getPrice()) == 0);

            if (isSamePrice) {
                // when multiple matches at same price, take earliest order
                matchedOrder = matches.get(0);
            } else {
                if (latestOrder.getOrderDirection() == OrderDirection.SELL) {
                    // when diff prices, take matching order with highest price
                    Optional<Order> highestPricedOrder = matches.stream()
                            .sorted((o1, o2) -> o2.getPrice().compareTo(o1.getPrice()))
                            .findFirst();

                    matchedOrder = highestPricedOrder.get();
                } else {
                    // when diff prices, take matching order with lowest price
                    Optional<Order> lowestPricedOrder = matches.stream()
                            .sorted(Comparator.comparing(Order::getPrice))
                            .findFirst();
                    matchedOrder = lowestPricedOrder.get();
                }
            }
        }

        orderPair[0] = matchedOrder;
        orderPair[1] = latestOrder;
        executedOrders.add(new ExecutedOrder(orderPair, latestOrder.getPrice()));
        openOrders.remove(matchedOrder);
    }

    private List<Order> getOrderMatches(Order order) {

        Objects.requireNonNull(order, INVALID_ARG_MSG);

        // Two orders match if they have opposing directions, matching RICs and quantities, and if the
        // sell price is less than or equal to the buy price

        List<Order> matches = openOrders.stream()
                .filter(o -> o.getRic().equals(order.getRic())
                        && isOppositeDirection(o, order)
                        && o.getQuantity() == order.getQuantity())
                .filter(o -> o.getOrderDirection() == OrderDirection.SELL && (o.getPrice().compareTo(order.getPrice()) < 1)
                        || (order.getOrderDirection() == OrderDirection.SELL && (order.getPrice().compareTo(o.getPrice()) < 1)))
                .collect(Collectors.toList());

        return matches;
    }

    private boolean isOppositeDirection(Order o1, Order o2) {
        return o1.getOrderDirection() != o2.getOrderDirection();
    }

    public Map<BigDecimal, Long> getOpenInterest(RIC ric, OrderDirection orderDirection) {

        return OrderReporting.getOpenInterest(ric, orderDirection, openOrders);
    }

    public BigDecimal getAveragePerUnitExecutionPrice(RIC ric) {

        return OrderReporting.getAveragePerUnitExecutionPrice(ric, executedOrders);
    }

    public long getExecutedQuantity(RIC ric, User user) {

        return OrderReporting.getExecutedQuantity(ric, user, executedOrders);
    }
}
