<html>
<head>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js"></script>
    <style>
        .boxed {
            border: 5px solid Gainsboro;
        }
        h1 h3 {
            font-family: "HelveticaNeue-Light", "Helvetica Neue Light", "Helvetica Neue", Helvetica, Arial, "Lucida Grande", sans-serif;
        }
        .button {
            background-color: #555555;
            border: none;
            color: white;
            padding: 1px 32px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
            font-size: 16px;
            margin: 4px 2px;
            cursor: pointer;
            width: 200px;
        }
    </style>
</head>

<body>

<h1>Feed API demo page</h1>

<div class="boxed">
    <h3>List All User(s)</h3>
    <button id="getAllUserButton" class="button">LIST ALL USERS</button>
    <h3 id="allusers"></h3>
</div>

<h1></h1>

<div class="boxed">
    <h3>List All Feed(s)</h3>
    <button id="getAllFeedButton" class="button">LIST ALL FEEDS</button>
    <h3 id="allfeeds"></h3>
</div>

<h1></h1>

<div class="boxed">
    <h3>Get Feeds that the User is subscribed to</h3>
    <input id="feeduser" placeholder="User...">
    <button id="getFeedButton" class="button">LIST FEEDS</button>
    <h3 id="feed"></h3>
</div>

<h1></h1>

<div class="boxed">
    <h3>Get Articles that the User has received
        (If a user does not subscribed to any feed, this API will return this user's historical articles)
    </h3>
    <input id="articlesuser" placeholder="User...">
    <button id="getArticleButton" class="button">LIST ARTICLES</button>
    <h3 id="article"></h3>
</div>

<h1></h1>

<div class="boxed">
    <h3>Create Feed</h3>
    <input id="feedid" placeholder="Feed...">
    <button id="createFeedButton" class="button">CREATE</button>
    <h3 id="publherandfeed"></h3>
</div>

<h1></h1>

<div class="boxed">
    <h3>Subscribe a User to Feed (If the user does not exist in this app yet, then this API will add this user into the app)</h3>
    <input id="subscribeid" placeholder="User...">
    <input id="feedname" placeholder="Feed...">
    <button id="subscribeUserFeed" class="button">SUBSCRIBE</button>
    <h3 id="allsubscribdfeeds"></h3>
</div>

<h1></h1>

<div class="boxed">
    <h3>Un-subscribe a User from Feed(If user doesn't exist then nothing happens, will return empty results)</h3>
    <input id="usertounsub" placeholder="User...">
    <input id="fromfeed" placeholder="Feed...">
    <button id="unsubscribeUserFromFeed" class="button">UN-SUBSCRIBE</button>
    <h3 id="updatedsubscribdfeeds"></h3>
</div>

<h1></h1>

<div class="boxed">
    <h3>Publish Article (If feed noes not exist then the nothing happens)</h3>
    <input id="feedpublisher" placeholder="Feed...">
    <input id="articleid" placeholder="Article...">
    <button id="publishArticleButton" class="button">PUBLISH</button>
    <h3 id="users"></h3>
</div>


<script>
    $(document).ready(function () {

        var base = "http://localhost:8080/feedreader/";

        function buildUrl(base, parameters) {
            return base.concat(parameters);
        }

        $("#getAllUserButton").click(function () {
            var URL = base.concat("users");

            $.get(URL, function (data, status) {
                $("#allusers").empty().append("All User(s) -- ".concat(data));
            });
        });

        $("#getAllFeedButton").click(function () {
            var URL = base.concat("feeds");

            $.get(URL, function (data, status) {
                $("#allfeeds").empty().append("All Feed(s) -- ".concat(data));
            });
        });

        $("#getFeedButton").click(function () {
            var feedsBaseUrl = base.concat("feeds/subscriber/");
            var user = $("#feeduser").val();
            $.get(buildUrl(feedsBaseUrl, user), function (data, status) {
                $("#feed").empty().append("Feed(s) id -- ".concat(data));
            });
        });

        $("#getArticleButton").click(function () {
            var feedsBaseUrl = base.concat("articles/subscriber/");
            var user = $("#articlesuser").val();
            $.get(buildUrl(feedsBaseUrl, user), function (data, status) {
                $("#article").empty().append("Article(s) id -- ".concat(data));
            });
        });

        $("#publishArticleButton").click(function () {
            var URL = base.concat("feed/").concat($("#feedpublisher").val()).concat("/article/").concat($("#articleid").val());
            $.get(URL, function (data, status) {
                $("#users").empty().append("Receiving user id(s) -- ".concat(data));
            });
        });

        $("#createFeedButton").click(function () {
            var URL = base.concat("create/feed/").concat($("#feedid").val());
            $.get(URL, function (data, status) {
                $("#publherandfeed").empty().append("All Feeds -- ".concat(data));
            });
        });

        $("#subscribeUserFeed").click(function () {
            var URL = base.concat("subscribe/").concat($("#subscribeid").val()).concat("/feed/").concat($("#feedname").val());
            $.get(URL, function (data, status) {
                $("#allsubscribdfeeds").empty().append("The user has subscribed : ".concat(data));
            });
        });

        $("#unsubscribeUserFromFeed").click(function () {
            var URL = base.concat("unsubscribe/").concat($("#usertounsub").val()).concat("/feed/").concat($("#fromfeed").val());
            $.get(URL, function (data, status) {
                $("#updatedsubscribdfeeds").empty().append("The user has subscribed : ".concat(data));
            });
        });

    });
</script>

</body>
</html>
