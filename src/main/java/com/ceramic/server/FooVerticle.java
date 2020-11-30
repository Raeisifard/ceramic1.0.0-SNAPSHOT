package com.ceramic.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

public class FooVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(FooVerticle.class);

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    log.info("Starting verticle {" + this + "}");
    HttpServer server = vertx.createHttpServer();

    final Router router = Router.router(vertx);
    router.route("/foo").handler(rc -> {
      rc.response().putHeader("ContentType", "text/html")
        .end("<html><body><strong>foo</strong></body></html>");
    });
    final Router main = ShareableRouter.router(vertx).mountSubRouter("/1", router);
    startFuture.complete();
    // start server
    /*server.requestHandler(main).listen(8888, lh -> {
      if (lh.failed()) {
        System.out.println("foo failed");
        startFuture.fail(lh.cause());
      } else {
        startFuture.complete();
      }
    });*/

  }

}
