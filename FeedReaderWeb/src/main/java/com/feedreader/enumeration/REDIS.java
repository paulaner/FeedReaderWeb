package com.feedreader.enumeration;

public enum REDIS {

    USER_ARTICLES(0),
    USER_FEEDS(1),
    FEED_PUBLISHER(2),
    FEED_USERS(3),
    PUBLISHER_LISTENER(4);

    private final int value;

    REDIS(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }
}
