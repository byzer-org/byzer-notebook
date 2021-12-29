package io.kyligence.notebook.console.support;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Profile("redis")
@EnableCaching
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 43200)//session过期时间(秒)
@Configuration
public class RedisSessionConfig
{
    @Bean
    public static ConfigureRedisAction configureRedisAction()
    {
            //让springSession不再执行config命令
            return ConfigureRedisAction.NO_OP;
    }
}
