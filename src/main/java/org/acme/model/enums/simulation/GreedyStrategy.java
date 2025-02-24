package org.acme.model.enums.simulation;

import resourceallocation.GreedyCriteria;

public enum GreedyStrategy {
    PROJECT_SIZE,ASSOCIATION_ACTIVITY,CREATION_DATE, UNKNOWN;

    public static GreedyCriteria toProto(GreedyStrategy protoEnum) {
        return switch (protoEnum) {
            case PROJECT_SIZE -> GreedyCriteria.PROJECT_SIZE;
            case ASSOCIATION_ACTIVITY -> GreedyCriteria.ASSOCIATION_ACTIVITY;
            case CREATION_DATE -> GreedyCriteria.CREATION_DATE;
            default -> GreedyCriteria.UNRECOGNIZED;
        };
    }
}
