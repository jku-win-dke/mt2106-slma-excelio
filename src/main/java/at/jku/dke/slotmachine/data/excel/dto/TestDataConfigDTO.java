package at.jku.dke.slotmachine.data.excel.dto;

import java.time.Instant;
/**
 * Displays the settings of TestDataGenerator -tdg
 */
public class TestDataConfigDTO {
	private int flightCount;
	private String flightPrefix;
	private int slotCount;
	private Instant slotStartTime;
	private int slotLengthSec; // in seconds
	private int marginWindowLength; // in seconds
	private int distributionSetting;
	private String optimizationFramework; //may be overwritten by -tdg
	/**
	 * [[0,20,0.8,1.2],[20,80,1.0,1.0],[80,100,2.0,2.0]]
	 * of each array entry, the first two values correspond to the percentile
	 * of the flights ([0,20,..] -> first 20% of flights)
	 * of each array entry, the third and fourth values correspond to the 
	 * value-area the priority can be in ([..,0.8,1.2] -> all priorities
	 * have to be between 0.8 and 1.2)
	 */
	private double[][] prioritySettings;
	private int minValue;
	private int maxValue;
	private int dropValue;
	private int maxTime; // in seconds
	
	public TestDataConfigDTO() {
		super();
	}
	
	public int getFlightCount() {
		return flightCount;
	}
	public void setFlightCount(int flightCount) {
		this.flightCount = flightCount;
	}
	public int getSlotCount() {
		return slotCount;
	}
	public void setSlotCount(int slotCount) {
		this.slotCount = slotCount;
	}
	public Instant getSlotStartTime() {
		return slotStartTime;
	}
	public void setSlotStartTime(Instant slotStartTime) {
		this.slotStartTime = slotStartTime;
	}
	public int getSlotLengthSec() {
		return slotLengthSec;
	}
	public void setSlotLengthSec(int slotLengthSec) {
		this.slotLengthSec = slotLengthSec;
	}
	public int getMarginWindowLength() {
		return marginWindowLength;
	}
	public void setMarginWindowLength(int marginWindowLength) {
		this.marginWindowLength = marginWindowLength;
	}
	public int getDistributionSetting() {
		return distributionSetting;
	}
	public void setDistributionSetting(int distributionSetting) {
		this.distributionSetting = distributionSetting;
	}
	public String getOptimizationFramework() {
		return optimizationFramework;
	}
	public void setOptimizationFramework(String optimizationFramework) {
		this.optimizationFramework = optimizationFramework;
	}
	public double[][] getPrioritySettings() {
		return prioritySettings;
	}
	public void setPrioritySettings(double[][] prioritySettings) {
		this.prioritySettings = prioritySettings;
	}
	public int getMinValue() {
		return minValue;
	}
	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}
	public int getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}
	public int getMaxTime() {
		return maxTime;
	}
	public void setMaxTime(int maxTime) {
		this.maxTime = maxTime;
	}

	public String getFlightPrefix() {
		return flightPrefix;
	}

	public void setFlightPrefix(String flightPrefix) {
		this.flightPrefix = flightPrefix;
	}

	public int getDropValue() {
		return dropValue;
	}

	public void setDropValue(int dropValue) {
		this.dropValue = dropValue;
	}
}
