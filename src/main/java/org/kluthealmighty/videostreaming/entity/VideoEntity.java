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

    @Column("video_path")
    private String path;

    @Column("created_at")
    private LocalDateTime createdAt;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public VideoEntity() {
    }

    public VideoEntity(String name, String description, String path) {
        this.name = name;
        this.description = description;
        this.path = path;
        this.createdAt = LocalDateTime.now();
    }
}
