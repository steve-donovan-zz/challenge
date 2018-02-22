package com.somebank.matcher;

import com.somebank.matcher.pojos.Order;
import com.somebank.matcher.pojos.OrderDirection;
import com.somebank.matcher.pojos.RIC;
import com.somebank.matcher.pojos.User;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static com.somebank.matcher.pojos.OrderDirection.*;

public class MatchingEngineTest {

    private final RIC VOD_L = new RIC("VOD.L");
    private final User USER_1 = new User("User1");
    private final User USER_2 = new User("User2");

    private final List<Order> orders = Arrays.asList(
            new Order(SELL, VOD_L, 1000l, new BigDecimal(100.2), USER_1),
            new Order(BUY, VOD_L, 1000l, new BigDecimal(100.2), USER_2),
            new Order(BUY, VOD_L, 1000l, new BigDecimal(99), USER_1),
            new Order(BUY, VOD_L, 1000l, new BigDecimal(101), USER_1),
            new Order(SELL, VOD_L, 500l, new BigDecimal(102), USER_2),
            new Order(BUY, VOD_L, 500l, new BigDecimal(103), USER_1),
            new Order(SELL, VOD_L, 1000l, new BigDecimal(98), USER_2)
    );

    private final MatchingEngine matchingEngine = new MatchingEngine();


    @Test
    public void WHEN_feeding_trades_THEN_open_interest_changes() {
        Map<BigDecimal, Long> result = null;

        // add new order
        Order newOrder = orders.get(0);
        matchingEngine.addOrder(newOrder);

        // open interest testing - both directions
        result = getOpenInterest(SELL);
        assertThat(result.size(), is(1));
        assertThat(result, hasEntry(newOrder.getPrice(), 1000L));

        result = getOpenInterest(BUY);
        assertThat(result.size(), is(0));

        // avg exec price changes
        assertThat(matchingEngine.getAveragePerUnitExecutionPrice(VOD_L), is(BigDecimal.ZERO));

        // executed quantity for user
        assertExecutedQuantity(USER_1, 0L);
        assertExecutedQuantity(USER_2, 0L);



        // add new order
        newOrder = orders.get(1);
        matchingEngine.addOrder(newOrder);

        // open interest testing - both directions
        result = getOpenInterest(SELL);
        assertThat(result.size(), is(0));

        result = getOpenInterest(BUY);
        assertThat(result.size(), is(0));

        // avg exec price changes
        assertAverageExecutionPrice(new BigDecimal(100.2000));

        // executed quantity for user
        assertExecutedQuantity(USER_1, -1000L);
        assertExecutedQuantity(USER_2, 1000L);



        // add new order
        newOrder = orders.get(2);
        matchingEngine.addOrder(newOrder);

        // open interest testing - both directions
        result = getOpenInterest(SELL);
        assertThat(result.size(), is(0));

        result = getOpenInterest(BUY);
        assertThat(result.size(), is(1));
        assertThat(result, hasEntry(newOrder.getPrice(), 1000L));

        // avg exec price changes
        assertAverageExecutionPrice(new BigDecimal(100.2000));

        // executed quantity for user
        assertExecutedQuantity(USER_1, -1000L);
        assertExecutedQuantity(USER_2, 1000L);



        // add new order
        newOrder = orders.get(3);
        matchingEngine.addOrder(newOrder);

        // open interest testing - both directions
        result = getOpenInterest(SELL);
        assertThat(result.size(), is(0));

        result = getOpenInterest(BUY);
        assertThat(result.size(), is(2));
        assertThat(result, hasEntry(newOrder.getPrice(), 1000L));
        assertThat(result, hasEntry(orders.get(2).getPrice(), 1000L));

        // avg exec price changes
        assertAverageExecutionPrice(new BigDecimal(100.2000));

        // executed quantity for user
        assertExecutedQuantity(USER_1, -1000L);
        assertExecutedQuantity(USER_2, 1000L);



        // add new order
        newOrder = orders.get(4);
        matchingEngine.addOrder(newOrder);

        // open interest testing - both directions
        result = getOpenInterest(SELL);
        assertThat(result.size(), is(1));
        assertThat(result, hasEntry(newOrder.getPrice(), 500L));

        result = getOpenInterest(BUY);
        assertThat(result.size(), is(2));
        assertThat(result, hasEntry(orders.get(2).getPrice(), 1000L));
        assertThat(result, hasEntry(orders.get(3).getPrice(), 1000L));

        // avg exec price changes
        assertAverageExecutionPrice(new BigDecimal(100.2000));

        // executed quantity for user
        assertExecutedQuantity(USER_1, -1000L);
        assertExecutedQuantity(USER_2, 1000L);



        // add new order
        newOrder = orders.get(5);
        matchingEngine.addOrder(newOrder);

        // open interest testing - both directions
        result = getOpenInterest(SELL);
        assertThat(result.size(), is(0));

        result = getOpenInterest(BUY);
        assertThat(result.size(), is(2));
        assertThat(result, hasEntry(orders.get(2).getPrice(), 1000L));
        assertThat(result, hasEntry(orders.get(3).getPrice(), 1000L));

        // avg exec price changes
        assertAverageExecutionPrice(new BigDecimal(101.1333));

        // executed quantity for user
        assertExecutedQuantity(USER_1, -500L);
        assertExecutedQuantity(USER_2, 500L);



        // add new order
        newOrder = orders.get(6);
        matchingEngine.addOrder(newOrder);

        // open interest testing - both directions
        result = getOpenInterest(SELL);
        assertThat(result.size(), is(0));

        result = getOpenInterest(BUY);
        assertThat(result.size(), is(1));
        assertThat(result, hasEntry(orders.get(2).getPrice(), 1000L));

        // avg exec price changes
        assertAverageExecutionPrice(new BigDecimal(99.8800));

        // executed quantity for user
        assertExecutedQuantity(USER_1, 500L);
        assertExecutedQuantity(USER_2, -500L);
    }


    // helper methods
    private Map<BigDecimal, Long> getOpenInterest(OrderDirection direction) {

        return matchingEngine.getOpenInterest(VOD_L, direction);
    }

    private void assertAverageExecutionPrice(BigDecimal expectedPrice) {

        assertThat(matchingEngine.getAveragePerUnitExecutionPrice(VOD_L), is(expectedPrice.setScale(4, RoundingMode.HALF_DOWN)));
    }

    private void assertExecutedQuantity(User user, long expectedQty) {

        assertThat(matchingEngine.getExecutedQuantity(VOD_L, user), is(expectedQty));
    }
}
