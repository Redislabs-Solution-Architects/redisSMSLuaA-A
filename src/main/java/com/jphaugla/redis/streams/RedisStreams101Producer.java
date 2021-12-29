package com.jphaugla.redis.streams;

import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RedisStreams101Producer {

    public final static String STREAMS_KEY = "weather_sensor:wind";

    public static void main(String[] args) {

        int nbOfMessageToSend = 1;

        if (args != null && args.length != 0 ) {
            nbOfMessageToSend = Integer.valueOf(args[0]);
        }

        System.out.println( String.format("\n Sending %s message(s)", nbOfMessageToSend));


        RedisClient redisClient = RedisClient.create("redis://localhost:12000"); // change to reflect your environment
        RedisClient redisClient2 = RedisClient.create("redis://localhost:12002");
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        StatefulRedisConnection<String, String> connection2 = redisClient2.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        RedisCommands<String, String> syncCommands2 = connection2.sync();

        for (int i = 0 ; i < nbOfMessageToSend ; i++) {

            Map<String, String> messageBody = new HashMap<>();
            messageBody.put("speed", "15");
            messageBody.put("direction", "270");
            messageBody.put("sensor_ts", String.valueOf(System.currentTimeMillis()));
            messageBody.put("loop_info", String.valueOf( i ));
            Boolean whichone = new Random().nextBoolean();
            String messageId = "";
            if (whichone) {
                messageBody.put("source_port", "12000");
                messageId = syncCommands.xadd(
                        STREAMS_KEY,
                        messageBody);
            } else {
                messageBody.put("source_port", "12002");
                 messageId = syncCommands2.xadd(
                    STREAMS_KEY,
                    messageBody);
            }

            System.out.println(String.format("\tMessage %s : %s posted", messageId, messageBody));
        }

        System.out.println("\n");

        connection.close();
        redisClient.shutdown();
        connection2.close();
        redisClient2.shutdown();

    }




}
