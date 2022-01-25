package com.jphaugla.redis.streams;

import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.List;
import java.util.Map;

public class RedisStreams101Consumer {

    public final static String STREAMS_KEY = "weather_sensor:wind";
    public final static String HASH_KEY = "weather_sensor:wind:hash:";
    public final static String MESSAGE_KEY = "weather_sensor:wind:message:";

    public static void main(String[] args) {

        String REDIS_URI=System.getenv("REDIS_URI");
        System.out.println("Redis URI=" + REDIS_URI);
        RedisClient redisClient = RedisClient.create(REDIS_URI);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        try {
            syncCommands.xgroupCreate( XReadArgs.StreamOffset.from(STREAMS_KEY, "0-0"), "application_1", XGroupCreateArgs.Builder.mkstream() );
        }
        catch (RedisBusyException redisBusyException) {
            System.out.printf("\t Group '%s' already exists%n", "application_1");
        }

        System.out.println("Waiting for new messages");

        while(true) {

            List<StreamMessage<String, String>> messages = syncCommands.xreadgroup(
                    Consumer.from("application_1", "consumer_1"),
                    XReadArgs.StreamOffset.lastConsumed(STREAMS_KEY)
            );

            if (!messages.isEmpty()) {
                for (StreamMessage<String, String> message : messages) {
                    System.out.println(message);
                    // Confirm that the message has been processed using XACK
                    syncCommands.xack(STREAMS_KEY, "application_1",  message.getId());
                    Map<String, String> body = message.getBody();
                    String hashKey = HASH_KEY + message.getId();
                    Double floatUp = Double.valueOf(body.get("floatUp"));
                    String messageId = body.get("message_id");
                    String messageKey = MESSAGE_KEY + messageId;
                    String numberParts = body.get("total_parts");
                    String thisPart = body.get("this_part");
                    //  write a hash for each message body
                    syncCommands.hmset(hashKey, body);
                    // syncCommands.hincrbyfloat(messageKey + ":float", "floatIncr", floatUp);
                    syncCommands.hincrby(messageKey + ":float", "totalParts", 1);
                    //  keep track of all the hash keys for this message body
                    syncCommands.sadd(messageKey, hashKey);
                    if (Integer.parseInt(numberParts) == Integer.parseInt(thisPart)) {
                        System.out.println("All Message parts received for " + messageKey);
                    }
                }
            }


        }


    }

}
