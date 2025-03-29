package com.nulnow.cloud.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class MainController {
    private ObjectMapper objectMapper = new ObjectMapper();

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:2375")
            .filters(exchangeFilterFunctions -> {
                exchangeFilterFunctions.add(logRequest());
//                exchangeFilterFunctions.add(logResponse());
            })
            .build();

    ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            StringBuilder sb = new StringBuilder("Request: \n");

            try {
                System.out.println(objectMapper.writeValueAsString(clientRequest));
            } catch (JsonProcessingException e) {
                System.out.println(e);
            }



            sb.append(clientRequest.url());
            sb.append("\n");
            sb.append(clientRequest.body());
            sb.append("\n");

            //append clientRequest method and url
            clientRequest
                    .headers()
                    .forEach((name, values) -> values.forEach(value -> {
                        sb.append(value);
                    }));

            System.out.println(sb.toString());

            return Mono.just(clientRequest);
        });
    }

//    ExchangeFilterFunction logResponse() {
//        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
//            return clientResponse.bodyToMono(String.class).flatMap(body -> {
//                StringBuilder sb = new StringBuilder("Response: \n");
//                sb.append("\n");
//                sb.append(clientResponse.statusCode());
//                sb.append("\n");
//
//                clientResponse.headers().asHttpHeaders()
//                        .forEach((header, value) -> {
//                            sb.append(header);
//                            sb.append(value.toString());
//                        });
//                sb.append("\n");
//                sb.append(body);
//                System.out.println(sb.toString());
//
//                return Mono.just(clientResponse);
//            });
//        });
//    }

    public record CreateAppRequest(
            String name,
            String repositoryUrl,
            String type
    ) {}

    @PostMapping(value = "/create-app", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> createApp(
            @RequestBody CreateAppRequest createAppRequest
    ) {
        if ("React SPA".equals(createAppRequest.type())) {
            String CONTAINER_PORT = "3000";
            String HOST_PORT = "80";
            String HOST_IP = String.format("%s.nulnow.com", createAppRequest.name());

            return webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1.48/containers/create")
                            .queryParam("name", createAppRequest.name())
                            .build()
                    )
                    .header("Content-Type", "application/json")
                    .bodyValue(
                            String.format("""
                                {
                                  "Image": "%s",
                                  "Env": [
                                    "REPOSITORY_URL=%s"
                                  ],
                                  "ExposedPorts": {
                                      "%s/tcp": {}
                                    },
                                  "HostConfig": {
                                    "PortBindings": {
                                      "%s/tcp": [
                                            {
                                            "HostIp": "%s",
                                            "HostPort": "%s"
                                          }
                                      ]
                                    }
                                  }
                                }
                                """, "dude/man:v2", createAppRequest.repositoryUrl(), CONTAINER_PORT, CONTAINER_PORT, HOST_IP, HOST_PORT)
                    )
                    .exchangeToMono(clientResponse -> {
                        return clientResponse.bodyToMono(JsonNode.class)
                                .flatMap(createContainerResponse -> {
                                    return webClient.post()
                                            .uri("/v1.48/containers/"+ createContainerResponse.get("Id").asText() +"/start")
                                            .retrieve()
                                            .bodyToMono(String.class)
                                            .map(result -> {
                                                return result;
                                            });
                                });
                    });
        } else if ("Spring Boot".equals(createAppRequest.type())) {
            String CONTAINER_PORT = "8080";
            String HOST_PORT = "8081";
            String HOST_IP = String.format("%s.nulnow.com", createAppRequest.name());

            return webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1.48/containers/create")
                            .queryParam("name", createAppRequest.name())
                            .build()
                    )
                    .header("Content-Type", "application/json")
                    .bodyValue(
                            String.format("""
                                {
                                  "Image": "%s",
                                  "Env": [
                                    "REPOSITORY_URL=%s"
                                  ],
                                  "ExposedPorts": {
                                      "%s/tcp": {}
                                    },
                                  "HostConfig": {
                                    "PortBindings": {
                                      "%s/tcp": [
                                            {
                                            "HostIp": "%s",
                                            "HostPort": "%s"
                                          }
                                      ]
                                    }
                                  }
                                }
                                """, "java", createAppRequest.repositoryUrl(), CONTAINER_PORT, CONTAINER_PORT, HOST_IP, HOST_PORT)
                    )
                    .exchangeToMono(clientResponse -> {
                        return clientResponse.bodyToMono(JsonNode.class)
                                .flatMap(createContainerResponse -> {
                                    return webClient.post()
                                            .uri("/v1.48/containers/"+ createContainerResponse.get("Id").asText() +"/start")
                                            .retrieve()
                                            .bodyToMono(String.class)
                                            .map(result -> {
                                                return result;
                                            });
                                });
                    });
        }

        throw new RuntimeException("TODO");
    }

    @GetMapping(value = "/list/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<JsonNode> listApps() {
        return webClient.get()
                .uri("/v1.48/containers/json")
                .retrieve()
                .bodyToFlux(JsonNode.class);
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<JsonNode> deleteApp(@PathVariable String id) {
        return webClient.delete()
                .uri("/v1.48/containers/" + id + "?force=true")
                .retrieve()
                .bodyToMono(JsonNode.class);
    }
}
