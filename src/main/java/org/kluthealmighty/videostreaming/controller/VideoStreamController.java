package org.kluthealmighty.videostreaming.controller;


import org.kluthealmighty.videostreaming.service.StreamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
//@RequestMapping("/api/v1/")
public class VideoStreamController {

    @Autowired
    private StreamingService streamingService;

    @GetMapping(value = "video/{title}", produces = "video/mp4")
    public Mono<Resource> getVideo(@PathVariable String title){
        return streamingService.getVideo(title);
    }
}
