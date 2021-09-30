package personal.davino.sensor.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
public interface SomeDatabaseService {


    static SomeDatabaseService createProxy(Vertx vertx,
                                           String address) {
        return new SomeDatabaseServiceVertxEBProxy(vertx, address);
    }

    // Actual service operations here...
    void save(String collection, JsonObject document,
              Handler<AsyncResult<Void>> resultHandler);
}