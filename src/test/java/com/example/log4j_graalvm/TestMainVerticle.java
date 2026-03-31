package com.example.log4j_graalvm;

import com.example.MainVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle()).onComplete(testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void test_http_response(Vertx vertx, VertxTestContext testContext) {
    vertx.createHttpClient()
        .request(HttpMethod.GET, 8888, "localhost", "/")
        .compose(req -> req.send())
        .onComplete(testContext.succeeding(response -> {
        assert response.statusCode() == 200;
        assert "text/plain".equals(response.getHeader("content-type"));
        response.body().onComplete(testContext.succeeding(content -> {
          assert "Hello from Vert.x!".equals(content.toString());
          testContext.completeNow();
        }));
      }));
  }
}
