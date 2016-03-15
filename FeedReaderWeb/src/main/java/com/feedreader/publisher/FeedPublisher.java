package com.feedreader.publisher;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.util.Date;
import java.util.Map;

public class FeedPublisher implements Runnable {
    private String url;
    private String user;
    private String password;
    private String topicName;
    private String publisherName;
    private String article;
    private volatile boolean shutdown;

    private ActiveMQConnectionFactory connectionFactory;

    public FeedPublisher(String topicName, String publishName, String article,
                         ActiveMQConnectionFactory connectionFactory) {
        this.topicName = topicName;
        this.publisherName = publishName;
        this.article = article;
        this.connectionFactory = connectionFactory;
        this.shutdown = false;
    }

    @Override
    public void run() {
        Session session = null;
        MessageProducer sendPublisher;
        Connection connection = null;
        while (!shutdown) {
            try {
                connection = connectionFactory.createConnection();
                connection.start();
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic topic = session.createTopic(this.topicName);
                sendPublisher = session.createProducer(topic);

                sendPublisher.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                sendPublisher.send(session.createTextMessage(article));

                //wait 1s make sure not to return before message is not sent yet
                //due to connection latency
                Thread.sleep(1000);
                this.shutdown = true;

                System.out.println("Publisher : " + publisherName + " sent Article : " + article);
            } catch (JMSException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
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

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }
}