package at.jku.dke.slotmachine.data.excel;

import java.time.Instant;

/**
 * Used for OptimizationSessionEntry
 */
public class SlotInfo {
		private Instant time;

		public SlotInfo() {
			
		}
		
		public SlotInfo(Instant time) {
			this.time = time;
		}

		public Instant getTime() {
			return time;
		}

		public void setTime(Instant time) {
			this.time = time;
		}
		
}
