package ch.sbb.cloud.autoscaler.model;

/**
 * Created by thomas on 01.09.16.
 */
public enum Metrics {

    SERVER_SIDE_RESPONSETIME(true),
    CLIENT_SIDE_RESPONSETIME(true),
    QUEUE_DEPTH(false),
    NUMBER_OF_HTTP_CONNECTIONS(false),
    MEMORY(false),
    CPU(true),
    THREAD_COUNT(false),
    DB_RESPONSE_TIME(true);

    private boolean isAggregate;

    Metrics(boolean isAggregate) {
        this.isAggregate = isAggregate;
    }

    public boolean isAggregate() {
        return isAggregate;
    }

}
