package com.ceramic.core;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
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
    retriever.getConfig(cnf ->  {
      JsonObject result = cnf.result();
      System.out.println("First Config:"+result.toString());
      // Close the vert.x instance, we don't need it anymore.
      retriever.close();
      vertx.close();

      // Create a new Vert.x instance using the retrieve configuration
      VertxOptions options = new VertxOptions(result);
      Vertx newVertx = Vertx.vertx(options);
      ConfigRetriever newRetriever = ConfigRetriever.create(newVertx, new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions().setType("file").setConfig(new JsonObject().put("path", "config.json"))));

      Future<JsonObject> future = newRetriever.getConfig();
      future.onComplete(ar -> {
        if (ar.failed()) {
          // Failed to retrieve the configuration
        } else {
          JsonObject config = ar.result();
          System.out.println("Second Config:"+config.toString());
        }
      });
      newRetriever.listen(change -> {
        JsonObject json = change.getNewConfiguration();
        newVertx.eventBus().publish("new-configuration", json);
        System.out.println("Config:"+json.toString());
      });
      // Deploy your verticle
      newVertx.deployVerticle(MainVerticle.class.getName(), new DeploymentOptions().setConfig(result));
    });
  }
}
