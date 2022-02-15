package io.kyligence.notebook.console.support;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

@Profile("redis")
@EnableCaching
@Configuration
public class RedisSessionConfig
{
    @Bean
    public static ConfigureRedisAction configureRedisAction()
    {
            //让springSession不再执行config命令
            return ConfigureRedisAction.NO_OP;
    }

    @EnableSpringHttpSession
    static class SessionOnRedis {
        @Bean
        public ZenRedisSessionRepository sessionRepository(RedisTemplate<Object, Object> redisTemplate) {
            RedisTemplate<Object, Object> newRedisTemplate = new RedisTemplate();
            newRedisTemplate.setKeySerializer(new StringRedisSerializer());
            newRedisTemplate.setHashKeySerializer(new StringRedisSerializer());
            newRedisTemplate.setConnectionFactory(redisTemplate.getConnectionFactory());
            newRedisTemplate.afterPropertiesSet();
            return new ZenRedisSessionRepository(newRedisTemplate, 3600l);
        }
    }
}
