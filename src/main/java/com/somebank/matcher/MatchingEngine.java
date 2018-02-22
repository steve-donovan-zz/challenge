package com.somebank.matcher;

import com.somebank.matcher.pojos.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class MatchingEngine {

    private ConcurrentLinkedQueue<Order> openOrders = new ConcurrentLinkedQueue();
    private ConcurrentLinkedQueue<ExecutedOrder> executedOrders = new ConcurrentLinkedQueue();


    public void addOrder(Order order) {

        Objects.requireNonNull(order, "Argument must not be null.");

        List<Order> matches = getOrderMatches(order);

        if (matches.size() > 0) {
            moveOrder(order, matches);
        } else {
            openOrders.add(order);
        }
    }

    private void moveOrder(Order latestOrder, List<Order> matches) {

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

        Objects.requireNonNull(order, "Argument must not be null.");

        // Two orders match if they have opposing directions, matching RICs and quantities, and if the
        // sell price is less than or equal to the buy price

        List<Order> matches = openOrders.stream()
                .filter(o -> o.getRic().equals(order.getRic())
                        && o.getOrderDirection() != order.getOrderDirection()
                        && o.getQuantity() == order.getQuantity())
                .filter(o -> o.getOrderDirection() == OrderDirection.SELL && (o.getPrice().compareTo(order.getPrice()) < 1)
                        || (order.getOrderDirection() == OrderDirection.SELL && (order.getPrice().compareTo(o.getPrice()) < 1)))
                .collect(Collectors.toList());

        return matches;
    }

    public Map<BigDecimal, Long> getOpenInterest(RIC ric, OrderDirection orderDirection) {
        return openOrders.stream()
                .filter(o -> o.getRic().equals(ric))
                .filter(o -> o.getOrderDirection() == orderDirection)
                .collect(Collectors.groupingBy(Order::getPrice, Collectors.summingLong(Order::getQuantity)));
    }

    public BigDecimal getAveragePerUnitExecutionPrice(RIC ric) {
        List<ExecutedOrder> matches = executedOrders.stream()
                .filter(o -> o.getRic().equals(ric))
                .collect(Collectors.toList());

        if (matches.size() == 0) {
            return BigDecimal.ZERO;
        }

        // (1000 * 100.2 + 500 * 103) / 1500
        long totalQuantity = 0;
        BigDecimal perUnitAvg = BigDecimal.ZERO;
        for (ExecutedOrder order : matches) {
            totalQuantity += order.getQuantity();
            perUnitAvg = perUnitAvg.add(order.getExecutedPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
        }

        return perUnitAvg.divide(new BigDecimal(totalQuantity), 4, RoundingMode.HALF_DOWN);
    }

    public long getExecutedQuantity(RIC ric, User user) {
        List<Order> orders = executedOrders.stream()
                .filter(o -> o.getRic().equals(ric))
                .flatMap(o -> Arrays.stream(o.getExecutedTrade()))
                .filter(o -> o.getUser().equals(user))
                .collect(Collectors.toList());

        long balance = 0;
        for (Order order : orders) {
            if (order.getOrderDirection() == OrderDirection.SELL) {
                balance -= order.getQuantity();
            } else {
                balance += order.getQuantity();
            }
        }
        return balance;
    }
}
