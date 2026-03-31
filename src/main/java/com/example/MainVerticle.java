package com.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class MainVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    })
    .listen(8888).onComplete(http -> {
      if (http.succeeded()) {
        logger.info("HTTP server started on port 8888");
      }
    })
    .compose(v -> checkJs())
    .onComplete(startPromise::handle);
  }

  Future<Void> checkJs() {
    return vertx.executeBlocking(() -> {
      check();
      return null;
    });
  }

  static void check() {
    check("x => {"
        + "  const r = new RegExp('[0-9]');"
        + "  return '1'.match(r) ? 1 : 0;"
        + "}");
  }

  static void check(String script) {
    Context context = Context.create("js");
    Value func = context.eval("js", script);
    Value value = func.execute();
    boolean ok = value.isNumber() && value.asLong() == 1L;
    context.close();
    if (!ok) {
      throw new IllegalStateException("Unexpected result from javascript engine");
    }
  }
}
