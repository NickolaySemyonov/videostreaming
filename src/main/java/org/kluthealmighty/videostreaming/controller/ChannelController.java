package org.kluthealmighty.videostreaming.controller;

import org.kluthealmighty.videostreaming.dto.ChannelDataRequest;
import org.kluthealmighty.videostreaming.dto.UserResponse;
import org.kluthealmighty.videostreaming.dto.VideoCard;
import org.kluthealmighty.videostreaming.security.JwtPrincipal;
import org.kluthealmighty.videostreaming.service.UserService;
import org.kluthealmighty.videostreaming.service.VideoCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ChannelController {

    @Autowired
    private UserService userService;

    @Autowired
    private VideoCardService videoCardService;

    @GetMapping("/channel/{channelTag}")
    public Mono<ResponseEntity<UserResponse>> getChannelData(@PathVariable String channelTag) {
        return userService.findUserByChannelTag(channelTag)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/channel/{channelTag}/video")
    public Flux<VideoCard> getAllChannelVideos(@PathVariable String channelTag){
        return videoCardService.getAllVideoCardsByChannelTag(channelTag);
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserResponse>> getOwnChannelData(@AuthenticationPrincipal JwtPrincipal principal) {
        return userService.findUserByChannelTag(principal.channelTag())
                .map(ResponseEntity::ok);
    }

    @GetMapping("/me/video")
    public Flux<VideoCard> getOwnChannelVideos(@AuthenticationPrincipal JwtPrincipal principal){
        return videoCardService.getAllVideoCardsByChannelTag(principal.channelTag());
    }

    @PutMapping("/me")
    public Mono<ResponseEntity<UserResponse>> updateOwnChannelData(
            @RequestPart("updateRequest") ChannelDataRequest request,
            @RequestPart(value = "banner", required = false) FilePart bannerPart,
            @RequestPart(value = "miniature", required = false) FilePart miniaturePart,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return userService.updateUser(principal.userId(), request, bannerPart, miniaturePart)
                .map(ResponseEntity::ok);
    }
}
