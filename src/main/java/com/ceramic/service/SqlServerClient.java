package com.ceramic.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.SQLRowStream;

import java.util.List;

public class SqlServerClient extends AbstractVerticle {
  private EventBus eb;
  private JsonObject setting = new JsonObject();
  private JsonObject clientConfig;
  private SQLClient client = null;
  private SQLConnection connection = null;
  private String queryName = "query", paramsName = "params", cmdName = "cmd", resultName = "result";
  private String query, cmd;
  private JsonArray params;
  private Boolean autoNext = false;
  private SQLRowStream sqlRowStream;
  private int rowCount = 0;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    this.eb = vertx.eventBus();
    setting = config().getJsonObject("data").getJsonObject("setting");
    JsonObject data = config().getJsonObject("data");
    JsonObject config = data.getJsonObject("config");
    JsonObject setting = data.getJsonObject("setting");
    this.query = setting.getString("query");
    String ip = config.getString("ip");
    Integer port = config.getInteger("port");
    String userName = config.getString("user");
    String password = config.getString("pass");
    String dbName = config.getString("dbName");
    String url = "jdbc:sqlserver://" + ip + ":" + port + ";SelectMethod=cursor;DatabaseName=" + dbName;
    clientConfig = new JsonObject()
      .put("url", url)
      .put("driver_class", "com.microsoft.sqlserver.jdbc.SQLServerDriver")
      .put("user", userName)
      .put("password", password)
      .put("max_pool_size", 30);
    client = JDBCClient.createShared(vertx, clientConfig);
    client.getConnection(res -> {
      if (res.succeeded()) {
        connection = res.result();
        //this.eb.consumer(this.ibmmqverticleid + ".trigger", this::trigger);
        //this.eb.consumer(this.ibmmqverticleid + ".input", this::input);
        startPromise.complete();
      } else {
        startPromise.fail(res.cause());
      }
    });
  }

  private <T> void input(Message<T> tMessage) {
  }

  private <T> void trigger(Message<T> tMessage) {
    this.cmdName = tMessage.headers().get("cmdName") == null ? this.cmdName : tMessage.headers().get("cmdName");
    this.queryName = tMessage.headers().get("queryName") == null ? this.queryName : tMessage.headers().get("queryName");
    this.resultName = tMessage.headers().get("resultName") == null ? this.resultName : tMessage.headers().get("resultName");
    this.paramsName = tMessage.headers().get("paramsName") == null ? this.paramsName : tMessage.headers().get("paramsName");
    JsonObject body;
    try {
      body = new JsonObject(tMessage.body().toString());
    } catch (Exception e) {
      //eb.publish(addressBook.getTrigger(), tMessage.body().toString(), addressBook.getDeliveryOptions(tMessage));
      return;
    }
    this.cmd = body.getString(this.cmdName, this.cmd);
    this.query = body.getString(this.queryName, this.query);
    this.params = body.getJsonArray(this.paramsName, this.params);
    this.autoNext = body.getBoolean("autoNext", this.autoNext);
    switch (cmd) {
      case "query":
        rowCount = 0;
        connection.queryStream(query, stream -> {
          if (stream.succeeded()) {
            sqlRowStream = stream.result();
            List<String> columns = sqlRowStream.columns();
            sqlRowStream
              .resultSetClosedHandler(v -> {
                // will ask to restart the stream with the new result set if any
                if (sqlRowStream != null)
                  sqlRowStream.moreResults();
              })
              .handler(row -> {
                sqlRowStream.pause();//Until cmd=next
                rowCount++;
                JsonObject result = new JsonObject();
                result.put("columns", columns).put("row", row).put("rowCount", rowCount);
                //this.eb.publish(addressBook.getResult(), result);
                //if (autoNext)
                  //this.eb.publish(this.ibmmqverticleid + ".trigger", new JsonObject().put("cmd", "next"));
              })
              .endHandler(v -> {
                // no more data available...
                // send info to Trigger port
                sqlRowStream.close();
                sqlRowStream = null;
                //this.eb.publish(addressBook.getError(), new JsonObject().put("msg", "result-set-end").put("rowCount", rowCount));
              });
          }
        });
        break;
      case "queryWithParams":
        rowCount = 0;
        break;
      case "querySingle":
        rowCount = 0;
        break;
      case "querySingleWithParams":
        rowCount = 0;
        break;
      case "update":
        break;
      case "updateWithParams":
        break;
      case "call":
        break;
      case "callWithParams":
        break;
      case "next":
        if (sqlRowStream != null) {
          sqlRowStream.resume();
        }
        break;
      default:
        //Unknown cmd!
        break;
    }
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {

  }
}
