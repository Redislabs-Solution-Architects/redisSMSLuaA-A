package com.jphaugla.redis.streams;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RedisStreams101ProducerSingle {

    public final static String STREAMS_KEY = "weather_sensor:wind";
    public final static String MESSAGE_KEY = "MSG";

    public static void main(String[] args) {

        int nbOfMessageToSend = 1;

        if (args != null && args.length != 0 ) {
            nbOfMessageToSend = Integer.parseInt(args[0]);
        }

        System.out.printf("\n Sending %s message(s)%n", nbOfMessageToSend);
        String REDIS_URI=System.getenv("REDIS_URI");
        System.out.println("Redis URI=" + REDIS_URI);
        RedisClient redisClient = RedisClient.create(REDIS_URI);

        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        for (int i = 0 ; i < nbOfMessageToSend ; i++) {

            Map<String, String> messageBody = new HashMap<>();
            messageBody.put("message_id", MESSAGE_KEY + i);
            int numberParts = new Random().nextInt(5) + 2;

            messageBody.put("speed", "15");
            messageBody.put("direction", "270");
            messageBody.put("sensor_ts", String.valueOf(System.currentTimeMillis()));
            for (int j = 1 ; j <= numberParts ; j++) {
                messageBody.put("total_parts", String.valueOf(numberParts));
                messageBody.put("this_part", String.valueOf(j));
                boolean whichRedis = new Random().nextBoolean();
                String messageId;
                messageId = syncCommands.xadd(
                            STREAMS_KEY,
                            messageBody);
               // System.out.printf("\tMessage %s : %s posted%n", messageId, messageBody);
            }

        }

        System.out.println("\n");

        connection.close();
        redisClient.shutdown();

    }




}
