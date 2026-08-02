package com.example.booking.dto;

import java.time.LocalDateTime;

public class RescheduleRequest {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public RescheduleRequest() {
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
