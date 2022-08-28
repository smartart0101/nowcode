package com.max;

import com.max.config.RedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

/**
 *
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void RedisTestString(){

        String rediskey = "test:count";

        //使用 redisTemplate 给字符串类型的 key: rediskey 赋值 1
        redisTemplate.opsForValue().set(rediskey,1);

        System.out.println(redisTemplate.opsForValue().get(rediskey));
        System.out.println(redisTemplate.opsForValue().increment(rediskey));
        System.out.println(redisTemplate.opsForValue().decrement(rediskey));

    }

    @Test
    public void testHashes(){
        String rediskey = "test:hash";

        //使用 redisTemplate 给xxx类型的 key: xxx 赋值
        redisTemplate.opsForHash().put(rediskey,"id",12);
        redisTemplate.opsForHash().put(rediskey,"username","345");

        System.out.println(redisTemplate.opsForHash().get(rediskey,"id"));
    }

    @Test
    public void testLists(){
        String rediskey = "test:list";

        //使用 redisTemplate 给xxx类型的 key: xxx 赋值,
        redisTemplate.opsForList().leftPush(rediskey,"111");
        redisTemplate.opsForList().rightPush(rediskey,"222");

        System.out.println(redisTemplate.opsForList().range(rediskey,0,1));
        System.out.println(redisTemplate.opsForList().leftPop(rediskey));
    }

    @Test
    public void testSets(){  //集合
        String rediskey = "test:sets";

        //使用 redisTemplate 给xxx类型的 key: xxx 赋值,
        redisTemplate.opsForSet().add(rediskey,"aaa","bbb","ccc");


        System.out.println(redisTemplate.opsForSet().size(rediskey));
        System.out.println(redisTemplate.opsForSet().pop(rediskey));
        System.out.println(redisTemplate.opsForSet().members(rediskey));
    }

    // 批量发送命令,节约网络开销.
    @Test
    public void testBoundOperations() {
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    // 编程式事务
    @Test
    public void testTransaction() {
        Object result = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "text:tx";

                // 启用事务
                redisOperations.multi();
                redisOperations.opsForSet().add(redisKey, "zhangsan");
                redisOperations.opsForSet().add(redisKey, "lisi");
                redisOperations.opsForSet().add(redisKey, "wangwu");

                System.out.println(redisOperations.opsForSet().members(redisKey));

                // 提交事务
                return redisOperations.exec();
            }
        });
        System.out.println(result);
    }

}
