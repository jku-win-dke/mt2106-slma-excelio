package at.jku.dke.slotmachine.data.excel;

import java.time.LocalDateTime;

public class OptimizationEntry {
    private String optId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int interval;
    private String optimizationFramework;
    /** 
     * gives the minimum value of weights in this optimization session
     */
    private double minValue;
    /**
     * gives the maximum value of weights in this optimization session
     */
    private double maxValue;
    /**
     * gives the value the weight values drop at TimeNotBefore / TimeNotAfter
     * (meaning, the values just before TimeNotBefore and at TimeNotBefore have
     *  a difference of dropValue; if dropValue = 60, then the value just before
     *  TimeNotBefore could be 0 and at TimeNotBefore it could be 60 then)
     */
    private double dropValue;

    public OptimizationEntry(String optId, LocalDateTime startTime, LocalDateTime endTime, int interval, String optimizationFramework,
    		double minValue, double maxValue, double dropValue) {
        this.optId = optId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.interval = interval;
        this.optimizationFramework = optimizationFramework;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.dropValue = dropValue;
    }

	public String getOptId() {
		return optId;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public int getInterval() {
		return interval;
	}

	public String getOptimizationFramework() {
		return optimizationFramework;
	}

	public double getMinValue() {
		return minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}
    public double getDropValue() {
    	return dropValue;
    }
    
}
