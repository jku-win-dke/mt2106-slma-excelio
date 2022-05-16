package at.jku.dke.slotmachine.data.excel;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlightGenerator {

	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Generates a list of Flight objects with a given margin list and an optimizationEntry
	 * @param margins list of margin entries
	 * @param opt optimizationEntry object
	 * @return list of Flight objects
	 */
	public static List<Flight> generateFlights(List<MarginEntry> margins, OptimizationEntry opt) {
		List<Slot> sequence = generateSequenceItems(opt);
		List<Flight> flights = new LinkedList<Flight>();
		
		for (MarginEntry margin: margins) {
			String flightId = margin.getFlightId();
			// uses time zone of vienna to get an Instant from LocalDateTime
			Instant scheduledTime = margin.getScheduledTime().atZone(ZoneId.of("Europe/Vienna")).toInstant();
			int[] weightMap = generateWeightMap(margin, sequence, opt);
			flights.add(new Flight(flightId, scheduledTime, weightMap));
		}
		
		return flights;
	}
	
	/**
	 * Generates sequence items (Slots)
	 * @param opt OptimizationEntry
	 * @return list of Slot objects
	 */
	public static List<Slot> generateSequenceItems(OptimizationEntry opt) {
		LocalDateTime startTime = opt.getStartTime();
		LocalDateTime endTime = opt.getEndTime();
		int interval = opt.getInterval();
		
		List<Slot> sequence = new LinkedList<Slot>();
		
		double totalSeconds = Duration.between(startTime, endTime).toSeconds();
		
		// adds elements in sequence with sequence number (0, 1, 2, ...) and
		// the time of the slot
		for (int i = 0; i <= totalSeconds; i = i + interval) {
			sequence.add(new Slot(i/interval, startTime.plusSeconds(i)));
		}
		
		return sequence;
	}
	
	/**
	 * Generates the weight maps according to the margins, min-/max-values and priority-value.
	 * @param margin contains information about the margins per flight
	 * @param sequence contains information about the possible slots
	 * @param opt contains information about slots, min-/max value and priority-value
	 * @return integer array with the generated weight map
	 */
	public static int[] generateWeightMap(MarginEntry margin, List<Slot> sequence, OptimizationEntry opt) {
		List<Double> weightMapList = new LinkedList<Double>();
		
		LocalDateTime timeSlotBegin = opt.getStartTime().atZone(ZoneId.of("Europe/Vienna")).toLocalDateTime();
		LocalDateTime timeSlotEnd = opt.getEndTime().atZone(ZoneId.of("Europe/Vienna")).toLocalDateTime();
		
		// calculate time durations between slot-begin-time and scheduledTime/TimeNotBefore/...
		// value can be negative or positive, Math.abs is not useful here with this cost function
		int intScheduledTime = (int) Duration.between(timeSlotBegin, margin.getScheduledTime()).toSeconds();
		if (intScheduledTime >= 0) { //for current requirements
			intScheduledTime = 0;
		}
		int intTimeNotBefore = (int) Duration.between(timeSlotBegin, margin.getTimeNotBefore()).toSeconds();
		if (intTimeNotBefore < 0) {
			intTimeNotBefore = 0;
		}
		int intTimeNotAfter = (int) Duration.between(timeSlotBegin, margin.getTimeNotAfter()).toSeconds();
		if (intTimeNotAfter < 0) {
			intTimeNotAfter = 0;
		}
		int intTimeWished = (int) Duration.between(timeSlotBegin, margin.getTimeWished()).toSeconds();
		if (intTimeWished < 0) {
			intTimeWished = 0;
		}
		int intSlotBegin = (int) Duration.between(timeSlotBegin, timeSlotBegin).toSeconds(); // always 0
		int intSlotEnd = (int) Duration.between(timeSlotBegin, timeSlotEnd).toSeconds();
		
		if (margin.getTimeNotBefore().isAfter(margin.getTimeWished()) 
				|| margin.getTimeWished().isAfter(margin.getTimeNotAfter())) {
			logger.info("TimeNotBefore must be before TimeWished and/or TimeWished must be before TimeNotAfter! " +
					"Therefore, Weight Map for flight " + margin.getFlightId() + " could have wrong values!");
		}
		
		for (Slot s: sequence) {
			double x = Math.abs(Duration.between(timeSlotBegin, s.getTime()).toSeconds());
			
			// before ScheduledTime, currently unused
			if (s.getTime().isBefore(margin.getScheduledTime())) {
				weightMapList.add(opt.getMinValue());
			// between ScheduledTime/SlotBegin (incl.) and TimeNotBefore (excl.)
			//		value at ScheduledTime or SlotStart = minValue
			//		value at TimeNotBefore = 0
			} else if (s.getTime().isBefore(margin.getTimeNotBefore()) 
					&& !(s.getTime().isBefore(margin.getScheduledTime())) ||
				(s.getTime().isBefore(margin.getTimeNotBefore()) && intScheduledTime == 0)) {
				double currentWeight = opt.getMinValue();
				// f(x) = k*x + minValue - k
				// k = (0 - minValue)/(TimeNotBefore-ScheduledTime) ODER (0 - minValue)/(TimeNotBefore - SlotBegin)
				double k = ((0 - opt.getMinValue())/(intTimeNotBefore - 0));
				if (intScheduledTime > 0) { //if ScheduledTime is after SlotBegin
					k = (Math.abs(opt.getMinValue())/(intTimeNotBefore - 0));
				}
				currentWeight = k * x + opt.getMinValue() - k;
				if (currentWeight < opt.getMinValue()) {
					currentWeight = opt.getMinValue();
				}
				weightMapList.add(currentWeight);
			// between TimeNotBefore (incl.) and TimeWished (excl.)
			//		value at TimeNotBefore = dropValue
			//		value at TimeNotBefore = maxValue
			} else if (s.getTime().isBefore(margin.getTimeWished()) 
					&& !(s.getTime().isBefore(margin.getTimeNotBefore()))) {
				double currentWeight = opt.getMinValue();
				// f(x) = k*x + d
				// k = (maxValue - dropValue)/(TimeWished - TimeNotBefore)
				double k = (opt.getMaxValue() - opt.getDropValue())/(intTimeWished - intTimeNotBefore);
				currentWeight = (k * x) + opt.getDropValue() - (k * intTimeNotBefore);
				weightMapList.add(currentWeight);
			// between TimeWished (incl.) and TimeNotAfter (incl.)
			//		value at TimeWished = maxValue
			//		value at TimeNotAfter = dropValue
			} else if (s.getTime().isBefore(margin.getTimeNotAfter()) 
					&& !(s.getTime().isBefore(margin.getTimeWished()))) {
				double currentWeight = opt.getMinValue();
				// f(x) = k*x + d
				// k = (dropValue - maxValue)/(TimeNotAfter - TimeWished)
				double k = (opt.getDropValue() - opt.getMaxValue())/(intTimeNotAfter - intTimeWished);
				currentWeight = (k * x) + opt.getMaxValue() - (k * intTimeWished);
				weightMapList.add(currentWeight);
			// between TimeNotAfter (incl.) and SlotEnd (incl.)
			//		value at TimeNotAfter = 0
			//		value at SlotEnd = minValue
			} else if (!(s.getTime().isBefore(margin.getTimeNotAfter()))) {
				double currentWeight = opt.getMinValue();
				// f(x) = k*x + d
				// k = (minValue - 0)/(SlotEnd - TimeNotAfter)
				double k = (opt.getMinValue())/(intSlotEnd - intTimeNotAfter);
				currentWeight = (k * x) + opt.getMinValue() - (k * intSlotEnd);
				if (currentWeight < opt.getMinValue()) {
					currentWeight = opt.getMinValue();
				}
				weightMapList.add(currentWeight);
			} else {
				weightMapList.add(opt.getMinValue());
			}
		}
		
		// convert used list to an array and multiply values by priority
		int[] weightMap = new int[weightMapList.size()];
		int i = 0;
		for (double value: weightMapList) {
			if (margin.getPriority() != 1) {
				weightMap[i] = (int) (value * margin.getPriority());
			} else {
				weightMap[i] = (int) value;
			}
			i++;
		}
		
		return weightMap;
	}
	

}
