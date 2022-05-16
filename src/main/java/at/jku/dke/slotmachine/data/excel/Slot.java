package at.jku.dke.slotmachine.data.excel;

import java.time.LocalDateTime;

public class Slot {
	private int sequenceNumber;
	private LocalDateTime time;
	
	public Slot(int sequenceNumber, LocalDateTime time) {
		super();
		this.sequenceNumber = sequenceNumber;
		this.time = time;
	}
	
	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public LocalDateTime getTime() {
		return time;
	}
	
}
