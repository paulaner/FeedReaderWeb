package com.feedreader.listener;


import com.feedreader.enumeration.REDIS;
import com.util.jedis.RedisUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class TextListener implements MessageListener {

    private String name = "";
    private String feed = "";

    public TextListener(String name, String feed) {
        this.name = name;
        this.feed = feed;
    }

    /**
     * Casts the message to a TextMessage and displays its text.
     *
     * @param message the incoming message
     */
    public void onMessage(Message message) {
        TextMessage msg = null;
        try {
            if (message instanceof TextMessage) {
                Jedis jedis = RedisUtil.getJedis();

                msg = (TextMessage) message;

                jedis.select(REDIS.FEED_USERS.getValue());
                List<String> users = jedis.lrange(feed, 0, jedis.llen(feed));

                //subscriber-articles
                jedis.select(REDIS.USER_ARTICLES.getValue());
                for (String user : users) {
                    List<String> articles;
                    if (jedis.exists(user)) {
                        articles = jedis.lrange(user, 0, jedis.llen(user));
                    } else {
                        articles = new ArrayList<>();
                    }
                    if (!articles.contains(msg.getText())) {
                        jedis.lpush(user, msg.getText());
                        System.out.println(user + " Read Article : " + msg.getText());
                    }
                }
                //return resource
                RedisUtil.returnJedis(jedis);
            } else {
                System.out.println("Message of wrong type: " + message.getClass().getName());
            }
        } catch (JMSException e) {
            System.out.println("JMSException in onMessage(): " + e.toString());
        } catch (Throwable t) {
            System.out.println("Exception in onMessage():" + t.getMessage());
        }
    }
}
