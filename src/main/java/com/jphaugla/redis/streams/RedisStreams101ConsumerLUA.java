package com.jphaugla.redis.streams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RedisStreams101ConsumerLUA {

    public final static String STREAMS_KEY = "weather_sensor:wind";
    public final static String HASH_KEY = "weather_sensor:wind:hash:";
    public final static String MESSAGE_KEY = "weather_sensor:wind:message:";

    public static void main(String[] args) throws JsonProcessingException {
        String portNumber = "12000";
        if (args != null && args.length != 0 ) {
            portNumber = args[0];
        }
        System.out.println("port number is " + portNumber);
        RedisClient redisClient = RedisClient.create("redis://localhost:" + portNumber); // change to reflect your environment
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
                    String messageId = body.get("message_id");
                    String messageKey = MESSAGE_KEY + messageId;
                    String numberParts = body.get("total_parts");
                    String thisPart = body.get("this_part");
                    // List<String> KEYS = Collections.singletonList(hashKey);
                    // List<Map<String, String>> ARGS = Collections.singletonList(body);
                    // syncCommands.evalsha(luaSHA, ScriptOutputType.STATUS, KEYS, body.toString());
                    String json = new ObjectMapper().writeValueAsString(body);
                    System.out.println(body.toString());
                    syncCommands.evalsha(luaSHA, ScriptOutputType.STATUS, Arrays.asList(hashKey).toArray(new String[0]), json);
                    //  write a hash for each message body
                    // syncCommands.hmset(hashKey, body);
                    //  keep track of all the hash keys for this message body
                    // syncCommands.sadd(messageKey, hashKey);
                    if (Integer.parseInt(numberParts) == Integer.parseInt(thisPart)) {
                        System.out.println("All Message parts received for " + messageKey);
                    }
                }
            }


        }

    }


}
