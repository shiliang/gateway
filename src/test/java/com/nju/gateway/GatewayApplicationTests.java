package com.nju.gateway;

import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GatewayApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void luaTest() {
        JedisPool jedisPool = new JedisPool("redis://47.102.135.76:6379");
        Jedis jedis = jedisPool.getResource();
        Reader reader = new InputStreamReader(Client.class.getClassLoader().getResourceAsStream("rateLimit.lua"));
        try {
            List<String> keys = new ArrayList<>();
            List<String> vals = new ArrayList<>();
            keys.add("localhost");
            vals.add("10000");
            vals.add("2");
            String luaScript = CharStreams.toString(reader);
            String scriptLoad = jedis.scriptLoad(luaScript);
            Object evalsha = jedis.evalsha(scriptLoad, keys, vals);
            System.out.println("aaa:"+evalsha);

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

}
