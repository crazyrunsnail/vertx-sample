package personal.davino.sensor;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Main {

    public static void main(String[] args) {
        System.setProperty("hazelcast.logging.type", "slf4j");
        Vertx.clusteredVertx(new VertxOptions())
                .onSuccess(vertx -> {
                    vertx.deployVerticle(new SensorVerticle());

            /*vertx.eventBus().<JsonObject>consumer("temperature.updates", message -> {
                JsonObject body = message.body();
                log.info("Consumer update >>> uuid: {}, temperature: {}", body.getString("uuid"), body.getFloat("temperature"));
            });*/

                }).onFailure(error -> {
                    log.error("woops", error);
                });
    }
}
