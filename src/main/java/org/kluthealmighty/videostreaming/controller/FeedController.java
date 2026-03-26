package org.kluthealmighty.videostreaming.controller;

import org.kluthealmighty.videostreaming.dto.ExtendedVideoCard;
import org.kluthealmighty.videostreaming.dto.VideoCard;
import org.kluthealmighty.videostreaming.security.JwtPrincipal;
import org.kluthealmighty.videostreaming.service.VideoCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/feed")
public class FeedController {

    @Autowired
    private VideoCardService videoCardService;

    @GetMapping
    public Flux<VideoCard> getAllVideos() {
        return videoCardService.getAllVideoCards();
    }

    @GetMapping("/{videoId}")
    public Mono<ExtendedVideoCard> getExtendedVideoCard(
            @PathVariable UUID videoId,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return videoCardService.getExtendedVideoCard(videoId, principal.userId());
    }
}
