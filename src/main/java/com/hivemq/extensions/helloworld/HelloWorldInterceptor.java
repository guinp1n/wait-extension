/*
 * Copyright 2018-present HiveMQ GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.extensions.helloworld;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.publish.PublishOutboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.publish.parameter.PublishOutboundInput;
import com.hivemq.extension.sdk.api.interceptor.publish.parameter.PublishOutboundOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This is a {@link PublishOutboundInterceptor},
 *
 * @author Dasha Samkova
 * @since 4.45.0
 */
public class HelloWorldInterceptor implements PublishOutboundInterceptor {
    private static final @NotNull Logger log = LoggerFactory.getLogger(HelloWorldInterceptor.class);
    final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onOutboundPublish(
            final @NotNull PublishOutboundInput publishOutboundInput,
            final @NotNull PublishOutboundOutput publishOutboundOutput) {

        //log.info("Client ID: {}",publishOutboundInput.getClientInformation().getClientId());
        // 1. Create a reusable OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // 2. Define the URL for the request
        final String port = System.getenv("HIVEMQ_REST_API_PORT");
        String url = "http://localhost:"+ port +"/api/v1/mqtt/clients/" + publishOutboundInput.getClientInformation().getClientId();

        // 3. Build the request object
        Request request = new Request.Builder()
                .url(url)
                .build(); // .get() is the default method

        // 4. Execute the request and handle the response
        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (response.isSuccessful()) {
                // Get the response body as a string (this will be your JSON)
                // Note: response.body().string() can be called only once.
            String jsonResponse = null;
            try {
                jsonResponse = response.body().string();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JsonNode root = null;
            try {
                root = objectMapper.readTree(jsonResponse);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            int messageQueueSize = root.path("client").path("messageQueueSize").asInt();
                int maxQueueSize = root.path("client").path("restrictions").path("maxQueueSize").asInt();

                log.info("HELLO Client ID: {}, queue: {}",publishOutboundInput.getClientInformation().getClientId(),messageQueueSize);
            }




    }
}