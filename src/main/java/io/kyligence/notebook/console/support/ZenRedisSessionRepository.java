package io.kyligence.notebook.console.support;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.MapSession;
import org.springframework.session.SessionRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public  class ZenRedisSessionRepository implements SessionRepository<MapSession> {

    private RedisTemplate sessionRedisOperations;
    private Long timeOut;
    private static final String KEY_PREFIX = "zen:session:";

    public ZenRedisSessionRepository(RedisTemplate<Object, Object> sessionRedisOperations, Long timeOut) {
        this.sessionRedisOperations = sessionRedisOperations;
        this.timeOut = timeOut;
    }


    private String getKey(String id){
        return KEY_PREFIX + id;
    }

    @Override
    public MapSession createSession() {
        MapSession result = new MapSession();
        result.setMaxInactiveInterval(Duration.ofSeconds((this.timeOut)));
        return result;
    }

    @Override
    public void save(MapSession session) {
        Map<String, Object> delta = new HashMap();

        delta.put("creationTime", session.getCreationTime().toEpochMilli());
        delta.put("maxInactiveInterval", (int)session.getMaxInactiveInterval().getSeconds());
        delta.put("lastAccessedTime", session.getLastAccessedTime().toEpochMilli());

        session.getAttributeNames().forEach((attributeName) -> {
            if (!"SPRING_SECURITY_CONTEXT".equals(attributeName)) {
                delta.put("sessionAttr:" + attributeName, session.getAttribute(attributeName));
            }
        });

        String key = getKey(session.getId());
        this.sessionRedisOperations.boundHashOps(key).putAll(delta);
        this.sessionRedisOperations.expire(key, timeOut,  TimeUnit.SECONDS);
    }

    private MapSession loadSession(String id, Map<Object, Object> entries) {
        MapSession loaded = new MapSession(id);
        Iterator var4 = entries.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<Object, Object> entry = (Map.Entry)var4.next();
            String key = (String)entry.getKey();
            if ("creationTime".equals(key)) {
                loaded.setCreationTime(Instant.ofEpochMilli((Long)entry.getValue()));
            } else if ("maxInactiveInterval".equals(key)) {
                loaded.setMaxInactiveInterval(Duration.ofSeconds((long)(Integer)entry.getValue()));
            } else if ("lastAccessedTime".equals(key)) {
                loaded.setLastAccessedTime(Instant.ofEpochMilli((Long)entry.getValue()));
            } else if (key.startsWith("sessionAttr:")) {
                loaded.setAttribute(key.substring("sessionAttr:".length()), entry.getValue());
            }
        }

        return loaded;
    }

    @Override
    public MapSession findById(String id) {
        String key = getKey(id);

        Map<Object, Object> entries = this.sessionRedisOperations.boundHashOps(key).entries();
        if (entries.isEmpty()) {
            return null;
        } else {
            MapSession session = this.loadSession(id, entries);
            if (session.isExpired()) {
                return null;
            }
            return session;
        }
    }

    @Override
    public void deleteById(String id) {
        String key = getKey(id);
        this.sessionRedisOperations.delete(key);
    }


}
