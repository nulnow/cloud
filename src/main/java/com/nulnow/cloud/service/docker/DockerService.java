package com.nulnow.cloud.service.docker;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class DockerService {


    @EventListener(ApplicationStartedEvent.class)
    public void test() {
//        createReactImage(null, "react-app")
//                .subscribe();
//        listImages().map(x -> {
//            x.stream().map(y -> y.get("RepoTags"));
//            return x;
//        }).subscribe();
    }
}