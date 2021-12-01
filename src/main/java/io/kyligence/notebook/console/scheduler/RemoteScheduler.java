package io.kyligence.notebook.console.scheduler;

import io.kyligence.notebook.console.exception.ByzerException;
import io.kyligence.notebook.console.exception.EngineAccessException;
import io.kyligence.notebook.console.exception.ErrorCodeEnum;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class RemoteScheduler {
    protected Integer serviceId;
    protected String serviceUrl;
    protected String serviceType;
    protected RestTemplate restTemplate;
    protected String callbackUrl;
    protected String callbackToken;

    public RemoteScheduler(Integer id, String serviceUrl, RestTemplate restTemplate,
                           String serviceType, String callbackUrl, String callbackToken){
        this.serviceId = id;
        this.serviceUrl = serviceUrl.replaceAll("/+$", "");
        this.restTemplate = restTemplate;
        this.serviceType = serviceType;
        this.callbackUrl = callbackUrl;
        this.callbackToken = callbackToken;
    }

    protected String genTaskDesc(String userName, String entityType, String entityName){
        return MessageFormat.format(
                "Byzer Schedule Create By User: [{0}] for {1}: {2}",
                userName,
                entityType,
                entityName
        );
    }

    protected String genTaskName(String userName, String entityType, Integer entityId) {
        return MessageFormat.format(
                "ByzerTask-{0}-{1}_{2}",
                userName,
                entityType,
                entityId
        );
    }

    protected String genTaskNamePrefix(String userName) {
        return Objects.isNull(userName) ? "ByzerTask-" : "ByzerTask-"+ userName + "-";
    }

    protected String post(String path, HttpHeaders headers, MultiValueMap<String, String> body) {
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    serviceUrl + path, request, String.class
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ByzerException();
            }
            return response.getBody();
        } catch (Exception e) {
            throw new ByzerException(ErrorCodeEnum.ENGINE_ACCESS_EXCEPTION, e);
        }
    }

    protected String get(String path, HttpHeaders headers) {
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    serviceUrl + path,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ByzerException();
            }
            return response.getBody();
        } catch (Exception e) {
            throw new ByzerException(ErrorCodeEnum.ENGINE_ACCESS_EXCEPTION, e);
        }

    }

    public String getName(){
        return serviceType + "-" + serviceId;
    }
}
