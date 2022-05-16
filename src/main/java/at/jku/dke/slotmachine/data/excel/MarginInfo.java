package at.jku.dke.slotmachine.data.excel;

import java.time.Instant;

/**
 * Stores information about margins, for usage in generated JSON file for Optimizer.
 */
public class MarginInfo {
	private String flightId;
    private Instant scheduledTime;
    private Instant timeNotBefore;
    private Instant timeWished;
    private Instant timeNotAfter; 
    
    public MarginInfo() {
    	super();
    	this.scheduledTime = null;
		this.timeNotBefore = null;
		this.timeWished = null;
		this.timeNotAfter = null;
    }
    
	public MarginInfo(Instant scheduledTime, Instant timeNotBefore, Instant timeWished, Instant timeNotAfter, String flightId) {
		super();
		this.scheduledTime = scheduledTime;
		this.timeNotBefore = timeNotBefore;
		this.timeWished = timeWished;
		this.timeNotAfter = timeNotAfter;
		this.flightId = flightId;
	}
	public Instant getScheduledTime() {
		return scheduledTime;
	}
	public void setScheduledTime(Instant scheduledTime) {
		this.scheduledTime = scheduledTime;
	}
	public Instant getTimeNotBefore() {
		return timeNotBefore;
	}
	public void setTimeNotBefore(Instant timeNotBefore) {
		this.timeNotBefore = timeNotBefore;
	}
	public Instant getTimeWished() {
		return timeWished;
	}
	public void setTimeWished(Instant timeWished) {
		this.timeWished = timeWished;
	}
	public Instant getTimeNotAfter() {
		return timeNotAfter;
	}
	public void setTimeNotAfter(Instant timeNotAfter) {
		this.timeNotAfter = timeNotAfter;
	}
	public String getFlightId() {
		return flightId;
	}
	public void setFlightId(String flightId) {
		this.flightId = flightId;
	}
    
}
