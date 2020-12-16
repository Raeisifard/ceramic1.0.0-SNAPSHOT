package com.ceramic.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;

public class SQLServer extends AbstractVerticle {
  private EventBus eb;

  @Override
  public void start(Promise<Void> startPromise) {
    String ip = config().getString("ip");
    Integer port = config().getInteger("port");
    String userName = config().getString("user");
    String password = config().getString("password");
    String dbName = config().getString("dbName");
    String url = "jdbc:sqlserver://" + ip + ":" + port + ";SelectMethod=cursor;DatabaseName=" + dbName;
    JsonObject config = new JsonObject()
      .put("url", url)
      .put("driver_class", "com.microsoft.sqlserver.jdbc.SQLServerDriver")
      .put("user", userName)
      .put("password", password)
      .put("max_pool_size", 30);
    JDBCPool pool = JDBCPool.pool(vertx, config);
    pool.query("SELECT TOP (1000) [Bill_Type]" +
      "      ,[Operation_Type]" +
      "      ,[Bill_ID]" +
      "      ,[Pay_ID]" +
      "      ,[Amount]" +
      "      ,[Org_Fee]" +
      "      ,[Iss_Fee]" +
      "      ,[IssInst]" +
      "      ,[AcqInst]" +
      "      ,[Payment_No]" +
      "      ,[Pay_Code]" +
      "      ,[Pay_Cap_Date]" +
      "      ,[PayTime]" +
      "      ,[Pan]" +
      "      ,[Account_From]" +
      "      ,[Code_Pri]" +
      "      ,[Code_Sec]" +
      "      ,[TimeIn]" +
      "      ,[DateIn]" +
      "      ,[RefNum]" +
      "      ,[TermID]" +
      "      ,[ID]" +
      "      ,[acctid2]" +
      "      ,[userid]" +
      "      ,[abmdate]" +
      "  FROM [tat_suny_new].[dbo].[Paid_Bill]")
      .execute()
      .onFailure(e -> {
        e.printStackTrace();
        // handle the failure
      })
      .onSuccess(rows -> {
        for (Row row : rows) {
          System.out.println(row.getString("Bill_ID"));
        }
      });
    startPromise.complete();
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    stopPromise.complete();
  }
}
