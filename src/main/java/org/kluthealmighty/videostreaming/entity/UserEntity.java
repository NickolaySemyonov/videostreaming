package org.kluthealmighty.videostreaming.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("users")
public class UserEntity {

    @Id //PK
    @Column("id")
    private Long id;

    @Column("email")
    private String email;

    @Column("password")
    private String password;

    @Column("channel_tag")
    private String channelTag;

    @Column("channel_name")
    private String channelName;

    @Column("channel_description")
    private String channelDescription;

    @Column("banner_path")
    private String bannerPath;

    @Column("miniature_path")
    private String miniaturePath;

    @Column("created_at")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getChannelTag() {
        return channelTag;
    }

    public void setChannelTag(String channelTag) {
        this.channelTag = channelTag;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelDescription() {
        return channelDescription;
    }

    public void setChannelDescription(String channelDescription) {
        this.channelDescription = channelDescription;
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

    public UserEntity(){}

    public UserEntity(Long id, String email, String password, String channelTag, String channelName, String channelDescription, String bannerPath, String miniaturePath, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.channelTag = channelTag;
        this.channelName = channelName;
        this.channelDescription = channelDescription;
        this.bannerPath = bannerPath;
        this.miniaturePath = miniaturePath;
        this.createdAt = createdAt;
    }
}
