package personal.davino.sensor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.UUID;

public class SensorVerticle extends AbstractVerticle {

    private final static Logger logger = LoggerFactory.getLogger(SensorVerticle.class);

    PgPool client;

    int httpPort = Integer.parseInt(System.getProperty("http.port", "8080"));

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        vertx.setPeriodic(3000, this::updateTemperature);

        this.initDataSource();
        this.recordData();

        vertx.createHttpServer().requestHandler(httpServerRequest -> {
            logger.info("Receive request from " + httpServerRequest.remoteAddress().host());
            httpServerRequest.response().end("hello from vertx");
        }).listen(httpPort, http -> {
            if (http.succeeded()) {
                logger.info("Service start success on port {}", httpPort);
                startPromise.complete();
            } else {
                logger.error("", http.cause());
                startPromise.fail("Service start fail >>");
            }
        });
    }

    private void initDataSource() {
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(5432)
                .setHost("127.0.0.1")
                .setDatabase("test")
                .setUser("postgres")
                .setPassword("postgres");

        // Pool options
        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);

        // Create the client pool
        client = PgPool.pool(vertx, connectOptions, poolOptions);
    }

    private void updateTemperature(Long time) {
        logger.info("Push temperature update >>>> ");
        vertx.eventBus().publish("temperature.updates",
                new JsonObject().put("uuid", UUID.randomUUID().toString())
                        .put("temperature", new Random().nextGaussian())
                        .put("timestamp", System.currentTimeMillis()));
    }

    private void recordData() {
        vertx.eventBus().<JsonObject>consumer("temperature.updates", message -> {
            JsonObject body = message.body();
            logger.info("Record data >>> uuid: {}, temperature: {}", body.getString("uuid"), body.getFloat("temperature"));
            Tuple tuple = Tuple.of(body.getString("uuid"), body.getFloat("temperature"),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(body.getLong("timestamp")), ZoneId.systemDefault()));
            client.preparedQuery("insert into record(uuid, temperature, ts) VALUES ($1, $2, $3)")
                    .execute(tuple).onSuccess(ok -> {
                        logger.info("Insert data success!");
            }).onFailure(cause -> {
                logger.info("Insert data fail!" , cause);
            });
        });
    }
}
