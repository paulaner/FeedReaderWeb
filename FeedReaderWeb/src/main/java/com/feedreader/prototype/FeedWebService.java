package com.feedreader.prototype;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedreader.enumeration.REDIS;
import com.feedreader.publisher.FeedPublisher;
import com.feedreader.subscriber.FeedSubscriber;
import com.util.jedis.RedisUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jredis.JredisConnectionFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class FeedWebService {

    @Autowired
    private ActiveMQConnectionFactory connectionFactory;

    private HashSet<String> oldPublishConsumer;

    /**
     * Subscribe the user to the given feed
     *
     * @param userId userId
     * @param feedId feed name
     * @throws Exception
     */
    public void subscribeOnFeed(String userId, String feedId) throws Exception {

        Jedis jedis = RedisUtil.getJedis();

        //insert new user-feed relation by inserting into these two dbs in Redis
        //subscriber-feed
        jedis.select(REDIS.USER_FEEDS.getValue());
        List<String> feeds;
        if (jedis.exists(userId)) {
            feeds = jedis.lrange(userId, 0, jedis.llen(userId));
        } else {
            feeds = new ArrayList<>();
        }
        if (!feeds.contains(feedId)) {
            jedis.lpush(userId, feedId);
            System.out.println("Subscriber : " + userId + " --> Feed : " + feedId);
        }

        //feed-subscriber
        jedis.select(REDIS.FEED_USERS.getValue());
        List<String> subscribers;
        if (jedis.exists(feedId)) {
            subscribers = jedis.lrange(feedId, 0, jedis.llen(feedId));
        } else {
            subscribers = new ArrayList<>();
        }
        if (!subscribers.contains(userId)) {
            jedis.lpush(feedId, userId);
            System.out.println("Feed : " + feedId + " --> Subscriber : " + userId);
        }

        //return resource
        RedisUtil.returnJedis(jedis);
    }

    /**
     * Remove a user from a feed
     *
     * @param userId user name
     * @param feedId feed name
     * @throws Exception
     */
    public void removeSubscription(String userId, String feedId) throws Exception {

        Jedis jedis = RedisUtil.getJedis();
        //subscriber-feeds
        jedis.select(REDIS.USER_FEEDS.getValue());
        jedis.lrem(userId, 1, feedId);

        //feed-subscribers
        jedis.select(REDIS.FEED_USERS.getValue());
        jedis.lrem(feedId, 1, userId);

        //return resource
        RedisUtil.returnJedis(jedis);
    }


    /**
     * Create a publisher's corresponding feed consumer and bind it to the feed
     *
     * @param consumer the feed consumer name
     * @param feedId     feedId
     * @throws Exception
     */
    private void createFeedConsumer(String consumer, String feedId) throws Exception {

        FeedSubscriber fs = new FeedSubscriber(feedId, consumer, connectionFactory);
        Thread t = new Thread(fs, consumer);
        t.start();

        if (this.oldPublishConsumer == null) {
            this.oldPublishConsumer = new HashSet<>();
        }

        this.oldPublishConsumer.add(consumer);

    }

    /**
     * Create a publisher and bind it to the given topic
     *
     * @param feedId feedId
     * @return all the current feed
     * @throws Exception
     */
    public String createFeed(String feedId) throws Exception {

        Jedis jedis = RedisUtil.getJedis();

        //publisher-feed
        jedis.select(REDIS.FEED_PUBLISHER.getValue());
        String publisherName = "Publisher_" + feedId;

        if (!jedis.exists(feedId)) {
            jedis.set(feedId, publisherName);
            System.out.println("Publisher : " + publisherName + " is created on Feed : " + feedId);
            System.out.println("Feed : " + feedId + " is created with Publisher : " + publisherName);

            String publishListener = "Subscriber_" + publisherName;
            jedis.select(REDIS.PUBLISHER_LISTENER.getValue());
            jedis.set(publisherName, publishListener);
            //create a corresponding topic subscriber for this topic
            createFeedConsumer(publishListener, feedId);
        }

        //return resource
        RedisUtil.returnJedis(jedis);

        return getAllFeeds();
    }

    /**
     * Publisher publishes an article to the feed that the publisher is binding to
     *
     * @param feedId    publisher name
     * @param articleId article unique id
     * @return All the users that could receive the article
     * @throws Exception
     */
    public String publish(String feedId, String articleId) throws Exception {

        Jedis jedis = RedisUtil.getJedis();

        jedis.select(REDIS.FEED_PUBLISHER.getValue());

        if(!jedis.exists(feedId)){
            throw new NullPointerException("Feed : " + feedId + " does not exist yet");
        }

        String publisher = jedis.get(feedId);

        //check if need to recover topic consumer
        //if this app just back from a restart, then oldPublishConsumer should be null or oldPublishConsumer
        //does not contains the subscriber
        if (oldPublishConsumer == null || !this.oldPublishConsumer.contains("Subscriber_" + publisher)) {
            createFeedConsumer("Subscriber_" + publisher, feedId);
        }

        jedis.select(REDIS.PUBLISHER_LISTENER.getValue());

        if (jedis.exists(publisher)) {
            FeedPublisher fs = new FeedPublisher(feedId, publisher, articleId, connectionFactory);
            Thread t = new Thread(fs, publisher);
            t.start();
            if (fs.isShutdown()) {
                //return resource
                RedisUtil.returnJedis(jedis);
            }
        }

        jedis.select(REDIS.FEED_USERS.getValue());
        List<String> users = jedis.lrange(feedId, 0, jedis.llen(feedId));
        ObjectMapper mapper = new ObjectMapper();

        //return resource
        RedisUtil.returnJedis(jedis);

        return mapper.writeValueAsString(users);

    }

    /**
     * Get all the feed the given user subscribes to
     *
     * @param userId user name
     * @return a list of feeds
     * @throws Exception
     */
    public String getFeeds(String userId) throws Exception {

        Jedis jedis = RedisUtil.getJedis();
        //subscriber-feeds
        jedis.select(REDIS.USER_FEEDS.getValue());

        ObjectMapper mapper = new ObjectMapper();
        String feeds = mapper.writeValueAsString(jedis.lrange(userId, 0, jedis.llen(userId)));

        //return resource
        RedisUtil.returnJedis(jedis);

        return feeds;
    }

    /**
     * Get all the articles that a user received
     *
     * @param userId userId
     * @return all the articles
     * @throws Exception
     */
    public String getArticles(String userId) throws Exception {

        Jedis jedis = RedisUtil.getJedis();
        //subscriber-articles
        jedis.select(REDIS.USER_ARTICLES.getValue());

        ObjectMapper mapper = new ObjectMapper();
        String articles = mapper.writeValueAsString(jedis.lrange(userId, 0, jedis.llen(userId)));

        //return resource
        RedisUtil.returnJedis(jedis);

        return articles;
    }

    /**
     * Get all the users
     *
     * @throws Exception
     */
    public String getAllUsers() throws Exception {

        Jedis jedis = RedisUtil.getJedis();
        //subscriber-feeds
        jedis.select(REDIS.USER_FEEDS.getValue());

        ObjectMapper mapper = new ObjectMapper();
        String users = mapper.writeValueAsString(jedis.keys("*"));

        //return resource
        RedisUtil.returnJedis(jedis);

        return users;
    }


    /**
     * Get all the feeds
     *
     * @throws Exception
     */
    public String getAllFeeds() throws Exception {

        Jedis jedis = RedisUtil.getJedis();
        //subscriber-feeds
        jedis.select(REDIS.FEED_PUBLISHER.getValue());
        Set<String> keys = jedis.keys("*");

        //return resource
        RedisUtil.returnJedis(jedis);

        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(keys);
    }

}
