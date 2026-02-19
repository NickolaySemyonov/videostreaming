package org.kluthealmighty.videostreaming.controller;

import org.kluthealmighty.videostreaming.DTO.VideoDTO;
import org.kluthealmighty.videostreaming.DTO.VideoUpdateDTO;
import org.kluthealmighty.videostreaming.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @GetMapping("/{id}")
    public ResponseEntity<VideoDTO> getVideo(@PathVariable UUID id){
        VideoDTO video = videoService.findVideoById(id);
        return ResponseEntity.status(HttpStatus.OK).body(video);
    }

    @GetMapping
    public ResponseEntity<List<VideoDTO>> getAllVideos(){
        List<VideoDTO> videos = videoService.findAllVideo();
        return ResponseEntity.status(HttpStatus.OK).body(videos);
    }

    @PostMapping
    public ResponseEntity<VideoDTO> createVideo(@RequestBody VideoDTO videoToCreate){
        VideoDTO createdVideo = videoService.createVideo(videoToCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVideo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VideoDTO> updateVideo(@PathVariable UUID id, @RequestBody VideoUpdateDTO videoToUpdate){
        VideoDTO updatedVideo = videoService.updateVideo(id, videoToUpdate);
        return ResponseEntity.status(HttpStatus.OK).body(updatedVideo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable UUID id){
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }
}
