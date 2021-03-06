package com.thienan.lovebox.payload.response;

import java.time.Instant;

public class BffRequestResponse {

    private Long id;
    private Instant createdAt;
    private UserBriefDetailResponse fromUser;
    private UserBriefDetailResponse toUser;
    private String text;

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

    public UserBriefDetailResponse getFromUser() {
        return fromUser;
    }

    public void setFromUser(UserBriefDetailResponse fromUser) {
        this.fromUser = fromUser;
    }

    public UserBriefDetailResponse getToUser() {
        return toUser;
    }

    public void setToUser(UserBriefDetailResponse toUser) {
        this.toUser = toUser;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
