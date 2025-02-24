package org.acme.model.enums.simulation;

import resourceallocation.GreedyCriteriaOrder;

public enum GreedyOrder {
    LARGEST_FIRST,SMALLEST_FIRST, UNKNOWN;

    public static GreedyCriteriaOrder toProto(GreedyOrder protoEnum) {
        return switch (protoEnum) {
            case LARGEST_FIRST -> GreedyCriteriaOrder.LARGEST_FIRST;
            case SMALLEST_FIRST -> GreedyCriteriaOrder.SMALLEST_FIRST;
            default -> GreedyCriteriaOrder.DEFAULT_ORDER;
        };
    }
}