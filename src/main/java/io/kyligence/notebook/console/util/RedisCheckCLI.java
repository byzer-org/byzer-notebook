/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kyligence.notebook.console.util;

import io.kyligence.notebook.console.NotebookConfig;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisAccessControlException;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

@Slf4j
public class RedisCheckCLI {
    public static void main(String[] args) {
        execute();
        Unsafe.systemExit(0);
    }

    public static void execute() {
        NotebookConfig config = NotebookConfig.getInstance();
        String redisHost = config.getRedisHost();
        String redisPort = config.getRedisPort();
        String redisDatabase = config.getRedisDatabase();
        String redisPassword = config.getRedisPassword();
        try (Jedis jedis = new Jedis(redisHost, Integer.parseInt(redisPort))) {
            String auth = jedis.auth(redisPassword);
            String ping = jedis.ping();
            System.out.println(ping);
        } catch (JedisConnectionException e) {
            System.out.println("ERROR: can not connect to redis, " +
                    "host:" + redisHost + " port:" + redisPort);
        } catch (JedisAccessControlException e) {
            System.out.println("ERROR: Wrong Password");
        } catch (JedisDataException e) {
            System.out.println("ERROR: Redis Without Password Configuration");
        } catch (Exception e) {
            System.out.println("ERROR: Unknown Error.");
        }
    }
}
