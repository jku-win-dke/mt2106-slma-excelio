package at.jku.dke.slotmachine.data.excel;

import java.util.List;

/**
 * Used to generate JSON-file to use for OptimizationDTO (see SlotMachine-Optimizer) 
 */
public class OptimizationSessionEntry {
    private String optId;
	private String[] initialFlightSequence;
	private Flight[] flights;
	private SlotInfo[] slots;
	private String optimizationFramework;
	/**
	 * margins (list of margins) can be NULL or contain information about the margins
	 */
	private List<MarginInfo> margins;
	
	public OptimizationSessionEntry() {
		
	}

	public OptimizationSessionEntry(String optId, String[] initialFlightSequence, Flight[] flights, SlotInfo[] slots,
			String optimizationFramework) {
		this.optId = optId;
		this.initialFlightSequence = initialFlightSequence;
		this.flights = flights;
		this.slots = slots;
		this.optimizationFramework = optimizationFramework;
		this.margins = null;
	}
	public OptimizationSessionEntry(String optId, String[] initialFlightSequence, Flight[] flights, SlotInfo[] slots,
			String optimizationFramework, List<MarginInfo> margins) {
		this.optId = optId;
		this.initialFlightSequence = initialFlightSequence;
		this.flights = flights;
		this.slots = slots;
		this.optimizationFramework = optimizationFramework;
		this.margins = margins;
	}

	public String getOptId() {
		return optId;
	}

	public String[] getInitialFlightSequence() {
		return initialFlightSequence;
	}

	public Flight[] getFlights() {
		return flights;
	}

	public SlotInfo[] getSlots() {
		return slots;
	}

	public String getOptimizationFramework() {
		return optimizationFramework;
	}

	public List<MarginInfo> getMargins() {
		return margins;
	}
	
	public void setMargins(List<MarginInfo> margins) {
		this.margins = margins;
	}
}
