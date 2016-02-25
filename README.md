# DECODE_URL default setting in immutant conflicts with compojure's URL decoding behavior.

To see this in action, in REPL:

```clojure
user> (require '[immutant.web :as web] '[test-decode-url.handler :as t] '[clj-http.client :as client] '[ring.util.codec :as codec] :reload)
nil
user> (def server (t/start-web))
#'user/server
user> (client/get (str "http://localhost:3000/" (codec/form-encode "%foo")))
ExceptionInfo clj-http: status 500  clj-http.client/wrap-exceptions/fn--19664 (client.clj:196)
```

The exception is

```java
11:22:22.438 ERROR [io.undertow.request] (XNIO-4 task-1) Undertow request failed HttpServerExchange{ GET /%25foo request {Connection=[close], accept-encoding=[gzip, deflate], User-Agent=[Apache-HttpClient/4.5.1 (Java/1.8.0_45)], Host=[localhost:3000]} response {Server=[undertow]}}
java.lang.NumberFormatException: For input string: "fo"
	at java.lang.NumberFormatException.forInputString(NumberFormatException.java:65) ~[na:1.8.0_45]
	at java.lang.Integer.parseInt(Integer.java:580) ~[na:1.8.0_45]
	at java.lang.Integer.valueOf(Integer.java:740) ~[na:1.8.0_45]
	at ring.util.codec$parse_bytes$fn__20079.invoke(codec.clj:42) ~[na:na]
	at clojure.core$map$fn__4553.invoke(core.clj:2624) ~[clojure-1.7.0.jar:na]
	at clojure.lang.LazySeq.sval(LazySeq.java:40) ~[clojure-1.7.0.jar:na]
	at clojure.lang.LazySeq.seq(LazySeq.java:49) ~[clojure-1.7.0.jar:na]
	at clojure.lang.RT.seq(RT.java:507) ~[clojure-1.7.0.jar:na]
	at clojure.lang.Numbers.byte_array(Numbers.java:1322) ~[clojure-1.7.0.jar:na]
	at ring.util.codec$parse_bytes.invoke(codec.clj:43) ~[na:na]
	at ring.util.codec$percent_decode$fn__20086.invoke(codec.clj:52) ~[na:na]
	at clojure.string$replace_by.invoke(string.clj:67) ~[clojure-1.7.0.jar:na]
	at clojure.string$replace.invoke(string.clj:106) ~[clojure-1.7.0.jar:na]
	at ring.util.codec$percent_decode.doInvoke(codec.clj:49) ~[na:na]
	at clojure.lang.RestFn.invoke(RestFn.java:423) ~[clojure-1.7.0.jar:na]
	at ring.util.codec$url_decode.doInvoke(codec.clj:69) ~[na:na]
	at clojure.lang.RestFn.invoke(RestFn.java:410) ~[clojure-1.7.0.jar:na]
	at ring.middleware.resource$resource_request.doInvoke(resource.clj:14) ~[na:na]
	at clojure.lang.RestFn.invoke(RestFn.java:442) ~[clojure-1.7.0.jar:na]
	at ring.middleware.resource$wrap_resource$fn__13589.invoke(resource.clj:27) ~[na:na]
	at ring.middleware.content_type$wrap_content_type$fn__13658.invoke(content_type.clj:30) ~[na:na]
	at ring.middleware.default_charset$wrap_default_charset$fn__13678.invoke(default_charset.clj:26) ~[na:na]
	at ring.middleware.not_modified$wrap_not_modified$fn__13639.invoke(not_modified.clj:52) ~[na:na]
	at ring.middleware.x_headers$wrap_xss_protection$fn__12258.invoke(x_headers.clj:71) ~[na:na]
	at ring.middleware.x_headers$wrap_frame_options$fn__12246.invoke(x_headers.clj:38) ~[na:na]
	at ring.middleware.x_headers$wrap_content_type_options$fn__12252.invoke(x_headers.clj:53) ~[na:na]
	at immutant.web.internal.undertow$create_http_handler$reify__15656.handleRequest(undertow.clj:135) ~[na:na]
	at org.projectodd.wunderboss.web.undertow.async.websocket.UndertowWebsocket$2.handleRequest(UndertowWebsocket.java:104) ~[wunderboss-web-undertow-0.11.0.jar:na]
	at io.undertow.server.session.SessionAttachmentHandler.handleRequest(SessionAttachmentHandler.java:68) ~[undertow-core-1.3.0.Beta9.jar:1.3.0.Beta9]
	at io.undertow.server.Connectors.executeRootHandler(Connectors.java:199) ~[undertow-core-1.3.0.Beta9.jar:1.3.0.Beta9]
	at io.undertow.server.HttpServerExchange$1.run(HttpServerExchange.java:778) ~[undertow-core-1.3.0.Beta9.jar:1.3.0.Beta9]
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) ~[na:1.8.0_45]
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617) ~[na:1.8.0_45]
	at java.lang.Thread.run(Thread.java:745) ~[na:1.8.0_45]
```

If you dig into the compojure code you can tell that it's trying to decode a string that is already decoded.

To fix, comment the current line starting the server, and uncomment the line that includes a builder configuration in the options:

```clojure
(defn start-web
  []
  (let [^Undertow$Builder builder
        (doto (Undertow/builder) (.setServerOption UndertowOptions/DECODE_URL false))]
    (web/run app (options {:port 3000 :configuration builder}))
    #_(web/run app (options {:port 3000}))))
```

Then back in the REPL:

```
user> (web/stop server)
true
user> (require '[immutant.web :as web] '[test-decode-url.handler :as t] '[clj-http.client :as client] '[ring.util.codec :as codec] :reload)
nil
user> (def server (t/start-web))
#'user/server
user> (client/get (str "http://localhost:3000/" (codec/form-encode "%foo")))
{:status 200, :headers {"Server" "undertow", "X-XSS-Protection" "1; mode=block", "X-Frame-Options" "SAMEORIGIN", "Date" "Thu, 25 Feb 2016 16:22:44 GMT", "Connection" "close", "X-Content-Type-Options" "nosniff", "Content-Type" "text/html; charset=utf-8", "Content-Length" "16"}, :body "Your param: %foo", :request-time 10, :trace-redirects ["http://localhost:3000/%25foo"], :orig-content-encoding nil, :cookies {"ring-session" {:discard true, :path "/", :secure false, :value "c1716dd9-d445-421f-b781-f2185e135d9f", :version 0}}}
user> 
```
