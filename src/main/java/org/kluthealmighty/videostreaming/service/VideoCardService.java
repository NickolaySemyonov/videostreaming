package org.kluthealmighty.videostreaming.service;

import org.kluthealmighty.videostreaming.dto.ExtendedVideoCard;
import org.kluthealmighty.videostreaming.dto.VideoCard;
import org.kluthealmighty.videostreaming.repository.UserRepository;
import org.kluthealmighty.videostreaming.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class VideoCardService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    public Flux<VideoCard> getAllVideoCards() {
        return videoRepository.findAll()
                .flatMap(video ->
                        userRepository.findById(video.getOwnerId())
                                .map(channel -> new VideoCard(
                                        video.getId(),
                                        video.getName(),
                                        video.getThumbnailPath(),
                                        channel.getChannelTag(),
                                        channel.getChannelName(),
                                        channel.getMiniaturePath()
                                ))
                );
    }

    public Flux<VideoCard> getAllVideoCardsByChannelTag(String channelTag) {
        return videoRepository.findAll()
                .flatMap(video ->
                        userRepository.findById(video.getOwnerId())
                                .map(channel -> new VideoCard(
                                        video.getId(),
                                        video.getName(),
                                        video.getThumbnailPath(),
                                        channel.getChannelTag(),
                                        channel.getChannelName(),
                                        channel.getMiniaturePath()
                                ))
                                .filter(videoCard -> videoCard.channelTag().equals(channelTag))
                );
    }


    public Mono<ExtendedVideoCard> getExtendedVideoCard(UUID videoId, Long principalId) {
        return videoRepository.findById(videoId)
                .flatMap(video ->
                        userRepository.findById(video.getOwnerId())
                                .map(channel -> new ExtendedVideoCard(
                                        video.getId(),
                                        video.getName(),
                                        video.getDescription(),
                                        video.getThumbnailPath(),
                                        video.getVideoPath(),
                                        channel.getChannelTag(),
                                        channel.getChannelName(),
                                        channel.getMiniaturePath(),
                                        video.getOwnerId().equals(principalId)
                                ))
                );

    }

}
