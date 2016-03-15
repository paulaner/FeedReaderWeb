package com.feedreader.prototype;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("")
public class FeedWebController {

    @Autowired
    private FeedWebService feedWebService;

    @RequestMapping(method = RequestMethod.GET)
    public String getIndex() throws Exception {
        return "index";
    }

    /**
     * Get All Users
     * <p/>
     * Return all users
     */
    @RequestMapping(value = "users", method = RequestMethod.GET)
    @ResponseBody
    public String getAllUsers() throws Exception {

        return feedWebService.getAllUsers();
    }

    /**
     * Get All Feeds
     * <p/>
     * Return all feeds
     */
    @RequestMapping(value = "feeds", method = RequestMethod.GET)
    @ResponseBody
    public String getAllFeeds() throws Exception {

        return feedWebService.getAllFeeds();
    }

    /**
     * Create a publisher and bind it to a given topic
     * <p/>
     * Return all current feeds
     */
    @RequestMapping(value = "create/feed/{feed}", method = RequestMethod.GET)
    @ResponseBody
    public String createFeed(
            @PathVariable(value = "feed") String feed) throws Exception {

        return feedWebService.createFeed(feed);
    }

    /**
     * Publish an article on the feed
     * <p/>
     * ReturnAll the users that could receive the article
     */
    @RequestMapping(value = "feed/{feedId}/article/{article}", method = RequestMethod.GET)
    @ResponseBody
    public String publish(
            @PathVariable(value = "feedId") String feedId,
            @PathVariable(value = "article") String article) throws Exception {

        return feedWebService.publish(feedId, article);
    }

    /**
     * Subscribe a user to the given feed
     * <p/>
     * Return all the feed that the user has subscribed to
     */
    @RequestMapping(value = "subscribe/{name}/feed/{feed}", method = RequestMethod.GET)
    @ResponseBody
    public String subscribeOnFeed(
            @PathVariable(value = "feed") String feed,
            @PathVariable(value = "name") String name)
            throws Exception {

        feedWebService.subscribeOnFeed(name, feed);

        return getFeeds(name);
    }

    /**
     * Unsubscribe a user from the given feed
     * <p/>
     * Return a string indicating that the Unsubscribe is done
     */
    @RequestMapping(value = "unsubscribe/{name}/feed/{feed}", method = RequestMethod.GET)
    @ResponseBody
    public String unsubscribeFromFeed(
            @PathVariable(value = "name") String name,
            @PathVariable(value = "feed") String feed) throws Exception {

        feedWebService.removeSubscription(name, feed);

        return getFeeds(name);
    }


    /**
     * Get all feeds which a given user subscribed to
     * <p/>
     * Return a list of feeds
     */
    @RequestMapping(value = "feeds/subscriber/{name}", method = RequestMethod.GET)
    @ResponseBody
    public String getFeeds(
            @PathVariable(value = "name") String name) throws Exception {

        return feedWebService.getFeeds(name);
    }

    /**
     * Get all articles that a user has received since this user is created
     * <p/>
     *
     * NOTE: If this app is down but the feeds keep getting new articles,
     *        when the app is back online, then those users will still get the articles that
     *        were published on the feed during the downtime
     *
     * Return a list of articles
     */
    @RequestMapping(value = "articles/subscriber/{name}", method = RequestMethod.GET)
    @ResponseBody
    public String getArticles(
            @PathVariable(value = "name") String name)
            throws Exception {

        return feedWebService.getArticles(name);
    }


}