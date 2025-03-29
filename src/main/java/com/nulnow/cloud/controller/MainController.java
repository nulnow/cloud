package com.nulnow.cloud.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import io.netty.channel.unix.DomainSocketAddress;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class MainController {
    public record CreateAppRequest(
            String name,
            String repositoryUrl
    ) {

    }

    @PostMapping(value = "/create-app", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> createApp(
            @RequestBody CreateAppRequest createAppRequest
    ) throws DockerCertificateException, DockerException, InterruptedException {
        // create ubuntu container
        var repositoryUrl = createAppRequest.repositoryUrl();
        // docker run --env REPOSITORY_URL=<value>

        final DefaultDockerClient docker = DefaultDockerClient.fromEnv().build();
        docker.pull("busybox");

// Bind container ports to host ports
        final String[] ports = {"80", "22"};
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        for (String port : ports) {
            List<PortBinding> hostPorts = new ArrayList<>();
            hostPorts.add(PortBinding.of("0.0.0.0", port));
            portBindings.put(port, hostPorts);
        }

// Bind container port 443 to an automatically allocated available host port.
        List<PortBinding> randomPort = new ArrayList<>();
        randomPort.add(PortBinding.randomPort("0.0.0.0"));
        portBindings.put("443", randomPort);

        final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

// Create container with exposed ports
        final ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image("busybox").exposedPorts(ports)
                .cmd("sh", "-c", "while :; do sleep 1; done")
                .build();

        final ContainerCreation creation = docker.createContainer(containerConfig);
        final String id = creation.id();

// Inspect container
        final ContainerInfo info = docker.inspectContainer(id);

// Start container
        docker.startContainer(id);

// Exec command inside running container with attached STDOUT and STDERR
        final String[] command = {"sh", "-c", "ls"};
        final ExecCreation execCreation = docker.execCreate(
                id, command, DefaultDockerClient.ExecCreateParam.attachStdout(),
                DefaultDockerClient.ExecCreateParam.attachStderr());
        final LogStream output = docker.execStart(execCreation.id());
        final String execOutput = output.readFully();

// Kill container
        docker.killContainer(id);

// Remove container
        docker.removeContainer(id);

// Close the docker client
        docker.close();

        return Mono.just(execOutput);

//        record BodyValue(
//                String dockerfile
//        ) {}

//        HttpClient httpClient = HttpClient.create()
//                .remoteAddress(() -> new DomainSocketAddress("/var/run/docker.sock"));
//
//        WebClient webClient = WebClient.builder()
//                .clientConnector(new ReactorClientHttpConnector(httpClient))
//                .baseUrl("http://localhost")
//                .build();
//
//        return webClient.get()
//                .uri("/version") // URI Docker API
//                .retrieve()
//                .bodyToMono(String.class);

//        return webClient.post()
//                .uri("/v1.41/containers/create")
//                .header("Content-Type", "application/json")
//                .bodyValue(
//                        "{\"Image\": \"alpine\", \"Cmd\": [\"echo\", \"hello world\"]}"
////                        new BodyValue("/Users/andrey/WebstormProjects/cloud/src/main/resources/piplanes/react-app-vite-typescript/Dockerfile")
//                )
//                .retrieve()
//                .bodyToMono(Response.class)
//                .map(response -> {
//                    return "ok";
//                });

//        curl -v -X POST -H "Content-Type:application/tar" --data-binary '@Dockerfile.tar.gz' http://127.0.0.1:5000/build?t=build_test

//        return Mono.just("ok");
    }

    @GetMapping(value = "/list/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> listApps() {
        return Mono.just("[]");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> deleteApp(@PathVariable String id) {
        return Mono.just("[]");
    }
}
