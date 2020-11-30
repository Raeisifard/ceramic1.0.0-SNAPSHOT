package com.ceramic.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    JsonArray verticleList = config().getJsonArray("verticles");
    vertx.deployVerticle(DeployerVerticle.class.getName(),
      new DeploymentOptions().setConfig(new JsonObject().put("verticles", verticleList)));
  }
}
