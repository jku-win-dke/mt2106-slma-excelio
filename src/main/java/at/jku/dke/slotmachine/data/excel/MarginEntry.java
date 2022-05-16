package at.jku.dke.slotmachine.data.excel;

import java.time.LocalDateTime;

public class MarginEntry {
    private String flightId;
    private LocalDateTime scheduledTime;
    private LocalDateTime timeNotBefore;
    private LocalDateTime timeWished;
    private LocalDateTime timeNotAfter;
    /**
     * 1.00 is the default priority; the weight will be multiplied by the priority
     */
    private double priority;

    public MarginEntry(String flightId, LocalDateTime scheduledTime, LocalDateTime timeNotBefore, LocalDateTime timeWished, LocalDateTime timeNotAfter, double priority) {
        this.flightId = flightId;
        this.scheduledTime = scheduledTime;
        this.timeNotBefore = timeNotBefore;
        this.timeWished = timeWished;
        this.timeNotAfter = timeNotAfter;
        this.priority = priority; 
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public LocalDateTime getTimeNotBefore() {
        return timeNotBefore;
    }

    public void setTimeNotBefore(LocalDateTime timeNotBefore) {
        this.timeNotBefore = timeNotBefore;
    }

    public LocalDateTime getTimeWished() {
        return timeWished;
    }

    public void setTimeWished(LocalDateTime timeWished) {
        this.timeWished = timeWished;
    }

    public LocalDateTime getTimeNotAfter() {
        return timeNotAfter;
    }

    public void setTimeNotAfter(LocalDateTime timeNotAfter) {
        this.timeNotAfter = timeNotAfter;
    }

	public double getPriority() {
		return priority;
	}

	public void setPriority(double priority) {
		this.priority = priority;
	}
}
