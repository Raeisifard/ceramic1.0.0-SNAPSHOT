package com.ceramic.core;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

public class StartConfig {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    // Create the config retriever
    ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("file").setConfig(new JsonObject().put("path", "config.json"))));

    // Retrieve the configuration
    retriever.getConfig(json ->  {
      JsonObject result = json.result();
      // Close the vert.x instance, we don't need it anymore.
      vertx.close();

      // Create a new Vert.x instance using the retrieve configuration
      VertxOptions options = new VertxOptions(result);
      Vertx newVertx = Vertx.vertx(options);

      // Deploy your verticle
      newVertx.deployVerticle(MainVerticle.class.getName(), new DeploymentOptions().setConfig(result));
    });
  }
}
