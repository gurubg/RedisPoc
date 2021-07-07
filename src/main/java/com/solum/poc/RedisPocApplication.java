package com.solum.poc;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Pipeline;

@SpringBootApplication
@Slf4j
@RestController
@RequestMapping("api/v1")
public class RedisPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisPocApplication.class, args);
	}

	@GetMapping(value = "/heartbeat")
	public String heartbeat()
	{
		return "Active" + System.currentTimeMillis();
	}
	 
	
	  @PostMapping(value = "/articles")
	   public void postArticles(@RequestParam(required = true) String appender) {
	    	
    	   JedisShardInfo shardInfo = new JedisShardInfo("guru.redis.cache.windows.net", 6380, true);
		    shardInfo.setPassword("LtpQfw1fLS39aXmQ09WOYFAAgW7zi6aRkrtgsb28ut0="); /* Use your access key. */
		    Jedis jedis = new Jedis(shardInfo);
      //  JedisPool jedisPool = new JedisPool("127.0.0.1", 6379);

       // Jedis jedis = jedisPool.getResource();
     //   jedis.psubscribe(new KeyExpiredListener(), "test");

        
        Pipeline pipeline = jedis.pipelined();
        
        Map<String, String> map = new HashMap<String, String>();

    	log.info("Article update started for key -" + appender);

    	//1000000
    	for(long i=1; i <= 1000000 ; i++)
    	{
    		
            String padded = String.format("%06d" , i);

    		String key = appender.concat(padded);

    		 map.put("1", "PRO");
    	     map.put("2", "PRO");
    	     map.put("3", "PRO");
    	  
    	     pipeline.hmset(key, map);
    	     //pipeline.expire(key, 100*60);
    		
    		if (i % 1000 == 0)
    		{
    			pipeline.sync();
    		}
    	
    	}
    	log.info("Article update finished for key -" + appender);
    	 jedis.close();
   	   // jedisPool.close();
    }
 
}
