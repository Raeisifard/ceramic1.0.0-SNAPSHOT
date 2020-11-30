package com.ceramic.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

public class HttpVerticle extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(HttpVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) {
    log.info("Starting verticle {" + this + "}");
    //create a router defining the endpoints of the service
    final Router router = Router.router(vertx);
    router.get("/one").handler(ctx -> ctx.response().end("OK one"));

    //mount the router as subrouter to the shared router
    final Router main = ShareableRouter.router(vertx).mountSubRouter("/1", router);

    vertx.createHttpServer().requestHandler(main).listen(8888, res -> {
      if (res.succeeded()) {
        //vertx.deployVerticle(new BarVerticle());
        //vertx.deployVerticle(new FooVerticle());
        startPromise.complete();
      } else {
        startPromise.fail(res.cause());
      }
    });
  }
}
