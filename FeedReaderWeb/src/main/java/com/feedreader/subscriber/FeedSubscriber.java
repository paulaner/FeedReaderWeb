package com.feedreader.subscriber;


import com.feedreader.listener.TextListener;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.util.Map;

public class FeedSubscriber implements Runnable {
    private String url;
    private String user;
    private String password;
    private String topicName;
    private String subName;

    private TopicSession localSession;
    private TopicSubscriber localSubscriber;
    private TopicConnection localConnection;
    private ActiveMQConnectionFactory connectionFactory;

    public FeedSubscriber(String topicName, String subName,
                          ActiveMQConnectionFactory connectionFactory) {
        this.topicName = topicName;
        this.subName = subName;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void run() {
        TopicSession session = null;
        TopicSubscriber subscriber = null;
        TopicConnection connection = null;
        try {
            connection = connectionFactory.createTopicConnection();
            connection.setClientID(subName);
            session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(this.topicName);

            subscriber = session.createDurableSubscriber(topic, subName);
            subscriber.setMessageListener(new TextListener(subName, topicName));
            connection.start();

            System.out.println("Subscriber : " + subName + " started listening Feed : " + topicName);

        } catch (JMSException e) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTopic() {
        return topicName;
    }

    public TopicSession getLocalSession() {
        return localSession;
    }

    public void setLocalSession(TopicSession localSession) {
        this.localSession = localSession;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public TopicSubscriber getLocalSubscriber() {
        return localSubscriber;
    }

    public void setLocalSubscriber(TopicSubscriber localSubscriber) {
        this.localSubscriber = localSubscriber;
    }

    public TopicConnection getLocalConnection() {
        return localConnection;
    }

    public void setLocalConnection(TopicConnection localConnection) {
        this.localConnection = localConnection;
    }
}

