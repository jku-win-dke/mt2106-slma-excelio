package at.jku.dke.slotmachine.data.excel;

import java.util.List;
import java.util.Map;

/**
 * Used to generate JSON-file to use for OptimizationDTO (see SlotMachine-Optimizer) 
 */
public class OptimizationSessionEntryAdvanced {
    private String optId;
	private String[] initialFlightSequence;
	private Flight[] flights;
	private SlotInfo[] slots;
	private String optimizationFramework;
	/**
	 * margins (list of margins) can be NULL or contain information about the margins
	 */
	private List<MarginInfo> margins;
	
	// allow configuration of jenetics or optaplanner
	private Map<String, Object> parameters;
	
	public OptimizationSessionEntryAdvanced() {
		
	}

	public OptimizationSessionEntryAdvanced(String optId, String[] initialFlightSequence, Flight[] flights, SlotInfo[] slots,
			String optimizationFramework) {
		this.optId = optId;
		this.initialFlightSequence = initialFlightSequence;
		this.flights = flights;
		this.slots = slots;
		this.optimizationFramework = optimizationFramework;
		this.margins = null;
	}
	public OptimizationSessionEntryAdvanced(String optId, String[] initialFlightSequence, Flight[] flights, SlotInfo[] slots,
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
	
	public void setOptId(String optId) {
		this.optId = optId;
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

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	public void setOptimizationFramework(String optimizationFramework) {
		this.optimizationFramework = optimizationFramework;
	}
}
