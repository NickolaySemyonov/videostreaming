package org.kluthealmighty.videostreaming.controller;

import org.kluthealmighty.videostreaming.dto.UpdateChannelRequest;
import org.kluthealmighty.videostreaming.dto.UserResponse;
import org.kluthealmighty.videostreaming.security.JwtPrincipal;
import org.kluthealmighty.videostreaming.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class ChannelController {

    @Autowired
    private UserService userService;

    @GetMapping("/channel/{channelTag}")
    public Mono<ResponseEntity<UserResponse>> getChannelData(@PathVariable String channelTag) {
        return userService.findUserByChannelTag(channelTag)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserResponse>> getOwnChannelData(@AuthenticationPrincipal JwtPrincipal principal) {
        return userService.findUserByChannelTag(principal.channelTag())
                .map(ResponseEntity::ok);
    }

    @PutMapping("/me")
    public Mono<ResponseEntity<UserResponse>> updateChannelData(
            @RequestPart("updateRequest") UpdateChannelRequest request,
            @RequestPart(value = "banner", required = false) FilePart bannerPart,
            @RequestPart(value = "miniature", required = false) FilePart miniaturePart,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return userService.updateUser(principal.userId(), request, bannerPart, miniaturePart)
                .map(ResponseEntity::ok);
    }
}
