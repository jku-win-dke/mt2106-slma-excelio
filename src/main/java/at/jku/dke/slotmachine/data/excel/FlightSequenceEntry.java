package at.jku.dke.slotmachine.data.excel;

public class FlightSequenceEntry {

	private String[] optimizedFlightSequence;
	private String optId;
	
	private Object slots;
	private Object margins;
	private Object fitness;
	
	public FlightSequenceEntry() {
		
	}
	
	public FlightSequenceEntry(String[] optimizedFlightSequence, String optId) {
		super();
		this.optimizedFlightSequence = optimizedFlightSequence;
		this.optId = optId;
	}
	
	public String[] getOptimizedFlightSequence() {
		return optimizedFlightSequence;
	}
	
	public void setOptimizedFlightSequence(String[] optimizedFlightSequence) {
		this.optimizedFlightSequence = optimizedFlightSequence;
	}
	
	public String getOptId() {
		return optId;
	}
	
	public void setOptId(String optId) {
		this.optId = optId;
	}

	public Object getSlots() {
		return slots;
	}

	public void setSlots(Object slots) {
		this.slots = slots;
	}

	public Object getMargins() {
		return margins;
	}

	public void setMargins(Object margins) {
		this.margins = margins;
	}

	public Object getFitness() {
		return fitness;
	}

	public void setFitness(Object fitness) {
		this.fitness = fitness;
	}
	
	
}
