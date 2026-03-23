package org.kluthealmighty.videostreaming.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;


@Table(name = "video")
public class VideoEntity {

    @Id //PK
    @Column("id")
    private UUID id;

    @Column("video_name")
    private String name;

    @Column("video_description")
    private String description;

    @Column("thumbnail_path")
    private String thumbnailPath;

    @Column("video_path")
    private String videoPath;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("owner_id")
    private Long ownerId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public VideoEntity() {
    }

    public VideoEntity(String name, String description, String thumbnailPath, String videoPath) {
        this.name = name;
        this.description = description;
        this.thumbnailPath = thumbnailPath;
        this.videoPath = videoPath;
        this.createdAt = LocalDateTime.now();
    }

    public VideoEntity(UUID id, String name, String description, String thumbnailPath, String videoPath, LocalDateTime createdAt, Long ownerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailPath = thumbnailPath;
        this.videoPath = videoPath;
        this.createdAt = createdAt;
        this.ownerId = ownerId;
    }
}
