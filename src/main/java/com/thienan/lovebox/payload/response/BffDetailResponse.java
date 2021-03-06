package com.thienan.lovebox.payload.response;

import java.time.Instant;

public class BffDetailResponse {

    private Long id;
    private Instant createdAt;
    private UserBriefDetailResponse firstUser;
    private UserBriefDetailResponse secondUser;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UserBriefDetailResponse getFirstUser() {
        return firstUser;
    }

    public void setFirstUser(UserBriefDetailResponse firstUser) {
        this.firstUser = firstUser;
    }

    public UserBriefDetailResponse getSecondUser() {
        return secondUser;
    }

    public void setSecondUser(UserBriefDetailResponse secondUser) {
        this.secondUser = secondUser;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
