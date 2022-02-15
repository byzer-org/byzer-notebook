/*
 *
 * Copyright (C) 2021 Kyligence Inc. All rights reserved.
 *
 * http://kyligence.io
 *
 * This software is the confidential and proprietary information of
 * Kyligence Inc. ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with
 * Kyligence Inc.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

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

    public static final String CREATION_TIME_KEY = "creationTime";
    public static final String MAX_INACTIVE_INTERVAL_KEY = "maxInactiveInterval";
    public static final String LAST_ACCESSED_TIME_KEY = "lastAccessedTime";
    public static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";
    public static final String SESSION_ATTR_PREFIX = "sessionAttr:";
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

        delta.put(CREATION_TIME_KEY, session.getCreationTime().toEpochMilli());
        delta.put(MAX_INACTIVE_INTERVAL_KEY, (int)session.getMaxInactiveInterval().getSeconds());
        delta.put(LAST_ACCESSED_TIME_KEY, session.getLastAccessedTime().toEpochMilli());

        session.getAttributeNames().forEach((attributeName) -> {
            if (!SPRING_SECURITY_CONTEXT_KEY.equals(attributeName)) {
                delta.put(SESSION_ATTR_PREFIX + attributeName, session.getAttribute(attributeName));
            }
        });

        String key = getKey(session.getId());
        this.sessionRedisOperations.boundHashOps(key).putAll(delta);
        this.sessionRedisOperations.expire(key, timeOut,  TimeUnit.SECONDS);
    }

    private MapSession loadSession(String id, Map<Object, Object> entries) {
        MapSession loaded = new MapSession(id);
        Iterator iterator = entries.entrySet().iterator();

        while(iterator.hasNext()) {
            Map.Entry<Object, Object> entry = (Map.Entry)iterator.next();
            String key = (String)entry.getKey();
            if (CREATION_TIME_KEY.equals(key)) {
                loaded.setCreationTime(Instant.ofEpochMilli((Long)entry.getValue()));
            } else if (MAX_INACTIVE_INTERVAL_KEY.equals(key)) {
                loaded.setMaxInactiveInterval(Duration.ofSeconds((long)(Integer)entry.getValue()));
            } else if (LAST_ACCESSED_TIME_KEY.equals(key)) {
                loaded.setLastAccessedTime(Instant.ofEpochMilli((Long)entry.getValue()));
            } else if (key.startsWith(SESSION_ATTR_PREFIX)) {
                loaded.setAttribute(key.substring(SESSION_ATTR_PREFIX.length()), entry.getValue());
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
        }
        MapSession session = this.loadSession(id, entries);
        if (session.isExpired()) {
            return null;
        }
        return session;
    }

    @Override
    public void deleteById(String id) {
        String key = getKey(id);
        this.sessionRedisOperations.delete(key);
    }


}
