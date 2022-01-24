package com.jphaugla.redis.streams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RedisStreams101ConsumerLUA {

    public final static String STREAMS_KEY = "weather_sensor:wind";
    public final static String HASH_INSIDE = ":hash:";
    //  to avoid cross slot error must use brackets so same hash slot
    public final static String MESSAGE_PREFIX = "{wind:message:";
    public final static String APPLICATION_NAME = "application_1";

    public static void main(String[] args) throws JsonProcessingException {

        String REDIS_URI=System.getenv("REDIS_URI");
        System.out.println("Redis URI=" + REDIS_URI);
        RedisClient redisClient = RedisClient.create(REDIS_URI);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        InputStream luaInputStream =
                RedisStreams101ConsumerLUA.class
                        .getClassLoader()
                        .getResourceAsStream("hmset.lua");
        String luaScript =
                new BufferedReader(new InputStreamReader(luaInputStream))
                        .lines()
                        .collect(Collectors.joining("\n"));

        String luaSHA = syncCommands.scriptLoad(luaScript);

        try {
            syncCommands.xgroupCreate( XReadArgs.StreamOffset.from(STREAMS_KEY, "0-0"), APPLICATION_NAME, XGroupCreateArgs.Builder.mkstream() );
        }
        catch (RedisBusyException redisBusyException) {
            System.out.printf("\t Group '%s' already exists%n", APPLICATION_NAME);
        }

        System.out.println("Waiting for new messages");

        while(true) {

            List<StreamMessage<String, String>> messages = syncCommands.xreadgroup(
                    Consumer.from(APPLICATION_NAME, "consumer_1"),
                    XReadArgs.StreamOffset.lastConsumed(STREAMS_KEY)
            );

            if (!messages.isEmpty()) {
                for (StreamMessage<String, String> message : messages) {
                    // System.out.println(message);
                    // Confirm that the message has been processed using XACK
                    // syncCommands.xack(STREAMS_KEY, APPLICATION_NAME,  message.getId());
                    Map<String, String> body = message.getBody();
                    String messageId = body.get("message_id");
                    String messageKey = MESSAGE_PREFIX + messageId + "}";
                    System.out.println(messageKey);
                    String hashKey = messageKey + HASH_INSIDE + message.getId();
                    // System.out.println(hashKey);
                    String numberParts = body.get("total_parts");
                    String thisPart = body.get("this_part");
                    String json = new ObjectMapper().writeValueAsString(body);
                    String[] KEYS = new String[3];
                    KEYS[0]=hashKey;
                    KEYS[1]=messageKey;
                    KEYS[2]=STREAMS_KEY;
                    syncCommands.evalsha(luaSHA, ScriptOutputType.STATUS, KEYS, json, APPLICATION_NAME, message.getId());
                    if (Integer.parseInt(numberParts) == Integer.parseInt(thisPart)) {
                        System.out.println("All Message parts received for " + messageKey);
                    }
                }
            }


        }

    }


}
