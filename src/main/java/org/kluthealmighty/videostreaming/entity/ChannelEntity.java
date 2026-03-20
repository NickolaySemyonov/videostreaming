package org.kluthealmighty.videostreaming.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("channel")
public class ChannelEntity {

    @Id
    @Column("id")
    private Long id;

    @Column("channel_tag")
    private String channelTag;

    @Column("banner_path")
    private String bannerPath;

    @Column("miniature_path")
    private String miniaturePath;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("owner_id")
    private Long ownerId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChannelTag() {
        return channelTag;
    }

    public void setChannelTag(String channelTag) {
        this.channelTag = channelTag;
    }

    public String getBannerPath() {
        return bannerPath;
    }

    public void setBannerPath(String bannerPath) {
        this.bannerPath = bannerPath;
    }

    public String getMiniaturePath() {
        return miniaturePath;
    }

    public void setMiniaturePath(String miniaturePath) {
        this.miniaturePath = miniaturePath;
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

    public ChannelEntity() {
    }

    public ChannelEntity(Long id, String channelTag, String bannerPath, String miniaturePath, LocalDateTime createdAt, Long ownerId) {
        this.id = id;
        this.channelTag = channelTag;
        this.bannerPath = bannerPath;
        this.miniaturePath = miniaturePath;
        this.createdAt = createdAt;
        this.ownerId = ownerId;
    }

}
