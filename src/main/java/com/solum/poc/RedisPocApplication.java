package com.solum.poc;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	    	
    	   JedisShardInfo shardInfo = new JedisShardInfo("guru.redis.cache.windows.net", 6379, false);
		    shardInfo.setPassword("zVgF5L0OJikHnUJVChwlvXzmES5UxG67h9RyiMwUkV4="); /* Use your access key. */
		    Jedis jedis = new Jedis(shardInfo);

		    System.out.println(shardInfo.getHost());

		    System.out.println(shardInfo.getPassword());
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
    	     pipeline.expire(key, 120*60);
    		
    		if (i % 1000 == 0)
    		{
    			pipeline.sync();
    		}
    	
    	}
    	log.info("Article update finished for key -" + appender);
    	 jedis.close();
   	   // jedisPool.close();
    }
 
	  public class TagStatus implements Runnable {

  		String appender;
  		String page;
  		int index;
  		int iNumSuccess = 0;
  	   public TagStatus(String  appender,String page,int index) {
  	       this.appender = appender;
  	      // this.page = page;
  	       this.index = index;
  	   }

  	   public void run() {
  		   
  		   JedisShardInfo shardInfo = new JedisShardInfo("guru.redis.cache.windows.net", 6379, false);
  		    shardInfo.setPassword("zVgF5L0OJikHnUJVChwlvXzmES5UxG67h9RyiMwUkV4="); /* Use your access key. */
  		    Jedis jedis = new Jedis(shardInfo);
  		   // jedis.set("foo", "bar");
  		    
          	log.info("Starting thread-" +appender + "-" );
//          	JedisPool jedisPool = new JedisPool("127.0.0.1", 6379);
//          	
//          	JedisPool jedisPool1 = new JedisPool(uri)
//
//              Jedis jedis = jedisPool.getResource();
              
              Pipeline pipeline = jedis.pipelined();

              Set<String> successKeys = new HashSet<>();
              
              for (int page = 1 ; page <= 3 ; page++)
              {
              for(long i= 1; i <= 1000000  ; i++)
              {

              	String padded = String.format("%06d" , i);

              	String key = appender.concat(padded );

              	jedis.hset(key, page + "", "ACK");
              	
              	
              	Map<String, String> map =  jedis.hgetAll(key);
              	
                 	boolean bSuccess = true;
      	        	
      		        if(map.values().contains("PRO"))
      		        {
      		        	bSuccess = false;
      		        }
      	        	if (bSuccess)
      	        	{
      	        		//Insert SUCCESS in DB 
      	        		//Delete the hash from Redis
      	        		//pipeline.del(key);
      	        		successKeys.add(key);
      	        		iNumSuccess++;
      	        	}
      	        	
      	        	if(iNumSuccess % 1000 == 0)
      	        	{
      	        	   successKeys.forEach(s -> pipeline.del(s) );
      	        	  // successKeys.forEach(s ->System.out.println(s) );
      	                pipeline.sync();
      	                successKeys.clear();
      	          //    System.out.println(successKeys.size());
      	        	}
              }
            
              }
          	
              jedis.close();
      	//    jedisPool.close();
      	    
      	    log.info("Closing thread - " + appender + " -" +(index+1) +  "--" + iNumSuccess);
  	   }
  	}

  
  @GetMapping(value = "/pageSuccess")
  public  void pageSuccess(@RequestParam(required = true) String appender,
  						@RequestParam(required = true) String page) {
  	
  	
  	Runnable r0 = new TagStatus(appender,page,0);
  	new Thread(r0).start();
  	
//  	Runnable r1 = new TagStatus(appender,page,1);
//  	new Thread(r1).start();
//  	
//  	Runnable r2 = new TagStatus(appender,page,2);
//  	new Thread(r2).start();
//  	
//  	Runnable r3 = new TagStatus(appender,page,3);
//  	new Thread(r3).start();
//  	
//  	Runnable r4 = new TagStatus(appender,page,4);
//  	new Thread(r4).start();
  	
 }
 
}
