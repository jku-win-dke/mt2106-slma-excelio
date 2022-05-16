package at.jku.dke.slotmachine.data.excel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.dke.slotmachine.data.excel.dto.TestDataConfigDTO;

public class TestDataGenerator {

	private static final Logger logger = LogManager.getLogger();
	/**
	 * Generates test data (JSON file(s))
	 * @param filesLocation location of configuration JSON file; generated JSON files will be stored in the same folder
	 * @param tdgConfig configuration for tdg, which contains:
	 * 	
	 * 			- flightCount, slotCount (should be the same number)<br>
	 * 			- flightPrefix<br>
	 * 			- slotStartTime, slotLengthSec (for the start of the slots and length of slots)<br>
	 * 			- marginWindowLength (in seconds)<br>
	 * 			- distributionSetting (which flight distribution setting should be used)<br>
	 * 			- optimizationFramework (this is usually overwritten)<br>
	 * 			- prioritySetting (how priority should be set for the flights)<br>
	 * 			- minValue, maxValue (for the weight map)<br>
	 * 			- maxTime (in seconds) (maximum time per optimization session)
	 */
	public static void generateTestData(String filesLocation, TestDataConfigDTO tdgConfig) {
		logger.info("Generating test data.");
		
		// possible inputs are in tdgConfig
		
		// check data inputs
		tdgConfig = checkInputs(tdgConfig);
		
		//generate jenConfig/optaPlannerConfig settings
			// set optimizationFramework accordingly! (here, optimizationFramework from tdgConfig is overwritten)
			// set sessionCount accordingly
		List<HashMap<String, Object>> jenConfigs = new LinkedList<HashMap<String, Object>>(); //size 16
		List<HashMap<String, Object>> optConfigs = new LinkedList<HashMap<String, Object>>(); //size 7
		jenConfigs = generateJenConfigs(tdgConfig.getMaxTime()); // 16 different configurations
		optConfigs = generateOptConfigs(tdgConfig.getMaxTime()); // 7 different configurations
		int sessionCount = 24;

		// common settings (initial flight sequence, slots, margins, flights, ...)
		UUID optId = UUID.randomUUID();
		String[] initialFlightSequence = generateInitialFlightSequence(tdgConfig.getFlightCount(), tdgConfig.getSlotCount(), tdgConfig.getFlightPrefix());
		SlotInfo[] slots = generateSlots(tdgConfig.getSlotCount(), tdgConfig.getSlotStartTime(), tdgConfig.getSlotLengthSec());
		
		List<Slot> slotList = convertSlotInfoToSlotList(slots);
		OptimizationEntry optE = new OptimizationEntry(optId.toString(), tdgConfig.getSlotStartTime().atZone(ZoneId.of("Europe/Vienna")).toLocalDateTime(),
				slots[slots.length-1].getTime().atZone(ZoneId.of("Europe/Vienna")).toLocalDateTime(), tdgConfig.getSlotLengthSec(), 
				tdgConfig.getOptimizationFramework(), tdgConfig.getMinValue(), tdgConfig.getMaxValue(), tdgConfig.getDropValue());
		
		// generate MarginEntries, then Flights, so that MarginEntries can be used later (for MarginInfos)
		MarginEntry[] margins = generateMargins(tdgConfig.getFlightCount(), slotList, tdgConfig.getMarginWindowLength(),
				tdgConfig.getDistributionSetting(), tdgConfig.getPrioritySettings(), tdgConfig.getFlightPrefix(), optE);
		Flight[] flights = generateFlights(tdgConfig.getFlightCount(), slotList, margins, 
				tdgConfig.getFlightPrefix(), optE);
		

		// look at generating jenConfig/optaPlannerConfig (?)
		//generateMarginInfo (contains Margins für later usage in Optimizer)
		// store data in JSON file
		logger.info("Set Margin-List with given input.");
		List<MarginInfo> marginInfos = new LinkedList<MarginInfo>();
		for (Flight f: flights) {
			MarginInfo currentMarginInfo = new MarginInfo();
			for (MarginEntry me: margins) {
				if (f.getFlightId().equals(me.getFlightId())) {
					currentMarginInfo.setFlightId(f.getFlightId());
					currentMarginInfo.setScheduledTime(me.getScheduledTime().atZone(ZoneId.of("Europe/Vienna")).toInstant());
					currentMarginInfo.setTimeNotBefore(me.getTimeNotBefore().atZone(ZoneId.of("Europe/Vienna")).toInstant());
					currentMarginInfo.setTimeWished(me.getTimeWished().atZone(ZoneId.of("Europe/Vienna")).toInstant());
					currentMarginInfo.setTimeNotAfter(me.getTimeNotAfter().atZone(ZoneId.of("Europe/Vienna")).toInstant());
				}
			}
			if (currentMarginInfo.getTimeNotAfter() != null) {
				marginInfos.add(currentMarginInfo);
			}
		}
		
		OptimizationSessionEntryAdvanced optEntry = new OptimizationSessionEntryAdvanced(optId.toString(),
				initialFlightSequence, flights, slots, tdgConfig.getOptimizationFramework());
		optEntry.setMargins(marginInfos);
		
		
		// repeat for every new optimization session
		for (int i = 0; i < sessionCount; i++) {
		//for (int i = 0; i < sessionCount; i++) {
			UUID currentOptId = UUID.randomUUID();
			optEntry.setOptId(currentOptId.toString());
			
			if (i < 16) {
				logger.info("Add jenetics configuration.");
				optEntry.setParameters(jenConfigs.get(i));
				optEntry.setOptimizationFramework("JENETICS");
				for (int k = 1; k <= 5; k++) { //generate 5 files per Jenetics configuration to allow multiple checks due to different results per same run
					currentOptId = UUID.randomUUID(); //generate seperate optIDs
					optEntry.setOptId(currentOptId.toString()); //generate seperate optIDs
					String fileName = filesLocation.substring(0,filesLocation.length()-5) + "-" + i + "-" + k + "--" + currentOptId.toString() + ".json";
					JsonOutputWriter.writeToFile(fileName, optEntry);
				}
			} else if (i < 23){ // for OptaPlanner require only one run per configuration, therefore no multiple files per configuration
				logger.info("Add optaplanner configuration.");
				optEntry.setParameters(optConfigs.get(i-16)); // i is at index 16 place ahead (due to jenConfigs size)
				optEntry.setOptimizationFramework("OPTAPLANNER");
				String fileName = filesLocation.substring(0,filesLocation.length()-5) + "-" + i + "--" + currentOptId.toString() + ".json";
				JsonOutputWriter.writeToFile(fileName, optEntry);
			} else {
				logger.info("Add Hungarian Algorithm configuration.");
				optEntry.setOptimizationFramework("HUNGARIAN");
				optEntry.setParameters(null);
				String fileName = filesLocation.substring(0,filesLocation.length()-5) + "-" + i + "--" + currentOptId.toString() + ".json";
				JsonOutputWriter.writeToFile(fileName, optEntry);
			}

//			String fileName = filesLocation.substring(0,filesLocation.length()-5) + "-" + i + "--" + currentOptId.toString() + ".json";
//			JsonOutputWriter.writeToFile(fileName, optEntry);
		}
		
		return;
	}

	/**
	 * Returns the given number as String with leading zeros according to max.
	 * 
	 * Examples: <br>
	 * - getNrString(12, 200) => return "012"<br>
	 * - getNrString(10, 10) => return "10"<br>
	 * - getNrString(5, 2345) => return "0005"
	 * @param nr
	 * @param max
	 * @return given number as String
	 */
	private static String getNrString(int nr, int max) {
		if (max < 0 || nr < 0) {
			return "0";
		}
		if (max < 10) {
			return Integer.valueOf(nr).toString();
		}
		if (max >= 10 && max < 100) {
			if (nr < 10) {
				return "0" + Integer.valueOf(nr).toString();
			} else { 
				return Integer.valueOf(nr).toString();
			}
		}
		if (max >= 100 && max < 1000) {
			if (nr < 10) {
				return "00" + Integer.valueOf(nr).toString();
			} else if (nr >= 10 && nr < 100) {
				return "0" + Integer.valueOf(nr).toString();
			} else {
				return Integer.valueOf(nr).toString();
			}
		}
		if (max >= 1000 && max < 10000) {
			if (nr < 10) {
				return "000" + Integer.valueOf(nr).toString();
			} else if (nr >= 10 && nr < 100) {
				return "00" + Integer.valueOf(nr).toString();
			} else if (nr >= 100 && nr < 1000) {
				return "0" + Integer.valueOf(nr).toString();
			} else {
				return Integer.valueOf(nr).toString();
			}
		}
		return Integer.valueOf(nr).toString();
	}
	
	/**
	 * Returns an initial flight sequence "F1, F2, F3, ..." according to flightcount and slotCount.
	 * @param flightCount
	 * @param slotCount
	 * @return initial flight sequence
	 */
	private static String[] generateInitialFlightSequence(int flightCount, int slotCount, String prefix) {
		if (flightCount < 0 || slotCount < 0) {
			logger.info("No initial flight sequence generated, as there are no available slots "
					+ " and/or no available flights.");
			return new String[0];
		}
		if (flightCount < slotCount) {
			logger.info("There are less flights than available slots.");
		}
		if (flightCount > slotCount) {
			logger.info("There are more flights than available slots. Therefore not all flights will be in "
					+ "the initial flight sequence.");
		}
		String[] initFlightSeq = new String[slotCount];
		// if there are more slots than flights, some slots will be null
		for (int i = 0; (i < slotCount && i < flightCount); i++) {
			initFlightSeq[i] = prefix + getNrString(i+1, slotCount);
		}
		return initFlightSeq;
	}
	
	/**
	 * Generate flights regarding parameters with margins
	 * @param flightCount amount of flights
	 * @param slots list of possible slots
	 * @param me MarginEntry object
	 * @param flightPrefix prefix (for flightId)
	 * @param optE OptimizationEntry object
	 * @return array of flights
	 */
	private static Flight[] generateFlights(int flightCount, List<Slot> slots, 
			MarginEntry[] me, String flightPrefix,
			OptimizationEntry optE) {
		// scheduledTime currently set to slotBegin for all flights
		Flight[] flights = new Flight[slots.size()];
		
		for (int i = 0; i < slots.size(); i++) {
			int nr = i + 1;
			String flightId = flightPrefix + nr;
			flights[i] = generateFlight(flightId, slots.get(0).getTime(), 
					me[i], slots, optE);
		}
		return flights;
	}
	
	/**
	 * Generate margin entries with the given input.
	 * @param flightCount amount of flights
	 * @param slots list of possible slots
	 * @param marginWindowWidth duration between TimeNotBefore and TimeNotAfter (in seconds)
	 * @param distributionSetting setting for distribution
	 * @param priority settings for priority
	 * @param flightPrefix prefix (for flightId)
	 * @param optE OptimizationEntry object
	 * @return array of margin entries
	 */
	private static MarginEntry[] generateMargins(int flightCount, List<Slot> slots, 
			int marginWindowWidth, int distributionSetting, double[][] priority, String flightPrefix,
			OptimizationEntry optE) {
		MarginEntry[] margins = new MarginEntry[slots.size()];
		switch (distributionSetting) {
			// setting 0: one flight per slot
			case 0:
				for (int i = 0; i < slots.size(); i++) {
					double priorityCurrent = getCurrentPriority(priority, i, slots.size());
					
					int nr = i + 1;
					String flightId = flightPrefix + nr;
					logger.debug("Flight " + flightId + ": priority = " + priorityCurrent);
					margins[i] = generateMargin(flightId, slots.get(0).getTime(), 
							marginWindowWidth, slots.get(i).getTime(), priorityCurrent, slots, optE);
				}
				return margins;
			// setting 1, 2, 3, 4 (distribution setting affects getFlightDistribution)
			default:
				double[] flightDistribution = getFlightDistribution(distributionSetting, slots.size());
				int[] flightMap = getFlightMap(flightCount, flightDistribution, slots, distributionSetting);
				int posMargins = 0;
				for (int i = 0; i < slots.size() && posMargins < slots.size(); i++) {
					double priorityCurrent = getCurrentPriority(priority, i, slots.size());
					
					for (int j = 0; j < flightMap[i] && posMargins < slots.size(); j++) {
						int nr = posMargins + 1;
						String flightId = flightPrefix + nr;
						margins[posMargins] = generateMargin(flightId, slots.get(0).getTime(), 
								marginWindowWidth, slots.get(i).getTime(), priorityCurrent, slots, optE);
						posMargins++;
					}
				}
				return margins;
		}
	}
	
	// helper method for generateMargins
	/**
	 * Sets flight distribution (the higher, the more flights will be at this particular slot).
	 * 
	 * Keep in mind, that low numbers may lead to no flights at all at the particular slots.
	 * @param i scenario of flight distribution
	 * @param size count of available slots
	 * @return array with 101 entries, for probability the current %-slot contains a flight or not (1=yes, 0=no)
	 */
	private static double[] getFlightDistribution(int i, int size) {
		double[] distribution = new double[0];
		/*if (i == 1) {
			// old scenario 1 is not suggested, as it is similar to scenario 0 in praxis (one flight per slot)
			distribution = new double[101];
			for (int j = 0; j <= 50; j++) {
				distribution[j] = 0.02 * j;
			}
			for (int j = 51; j < 101; j++) {
				distribution[j] = ((-1) * 0.02 * j) + 2;
			}
		}*/
		if (i == 1) {
			// scenario 1
				// most flights in the middle (100 flights at 100 slots)
			distribution = new double[100];
			distribution[0] = 1;
			distribution = fillArraySlots(distribution, 1, 3, 0);
			distribution[3] = 1;
			distribution = fillArraySlots(distribution, 4, 6, 0);
			distribution[6] = 1;
			distribution = fillArraySlots(distribution, 7, 9, 0);
			distribution[9] = 1;
			distribution = fillArraySlots(distribution, 10, 12, 0);
			distribution[12] = 1;
			distribution = fillArraySlots(distribution, 13, 15, 0);
			distribution[15] = 1;
			distribution = fillArraySlots(distribution, 16, 18, 0);
			distribution[18] = 1;
			distribution = fillArraySlots(distribution, 19, 21, 0);
			distribution[21] = 1;
			distribution = fillArraySlots(distribution, 22, 24, 0);
			distribution[24] = 1;
			distribution = fillArraySlots(distribution, 25, 27, 0);
			distribution[27] = 1;
			distribution = fillArraySlots(distribution, 28, 29, 0);
			distribution = fillArraySlots(distribution, 29, 44, 1);
			distribution = fillArraySlots(distribution, 40, 46, 2);
			distribution[46] = 2;
			distribution[47] = 3;
			distribution[48] = 3;
			distribution[49] = 5;
			distribution[50] = 9;
			distribution[51] = 5;
			distribution[52] = 3;
			distribution[53] = 3;
			distribution[54] = 2;
			distribution = fillArraySlots(distribution, 55, 61, 2);
			distribution = fillArraySlots(distribution, 61, 72, 1);
			distribution[72] = 1;
			distribution = fillArraySlots(distribution, 73,75, 0);
			distribution[75] = 1;
			distribution = fillArraySlots(distribution, 76, 78, 0);
			distribution[78] = 1;
			distribution = fillArraySlots(distribution, 79, 81, 0);
			distribution[81] = 1;
			distribution = fillArraySlots(distribution, 82, 84, 0);
			distribution[84] = 1;
			distribution = fillArraySlots(distribution, 85, 87, 0);
			distribution[87] = 1;
			distribution = fillArraySlots(distribution, 88, 90, 0);
			distribution[90] = 1;
			distribution = fillArraySlots(distribution, 91, 93, 0);
			distribution[93] = 1;
			distribution = fillArraySlots(distribution, 94, 96, 0);
			distribution[96] = 1;
			distribution = fillArraySlots(distribution, 97, 99, 0);
			distribution[99] = 1;
		}
		if (i == 2) {
			// scenario 2 (modified scenario 1)
				// most flights at the beginning and the end of the available slots
			distribution = new double[100];
			distribution[0] = 8;
			distribution[1] = 4;
			distribution[2] = 3;
			distribution[3] = 2;
			distribution[4] = 2;
			distribution[5] = 2;
			distribution = fillArraySlots(distribution, 6, 11, 1);
			distribution = fillArraySlots(distribution, 11, 21, 1);
			distribution = fillArraySlots(distribution, 21, 80, 1, 2);
			distribution = fillArraySlots(distribution, 80, 89, 1);
			distribution = fillArraySlots(distribution, 89, 94, 1);
			distribution[94] = 2;
			distribution[95] = 2;
			distribution[96] = 2;
			distribution[97] = 3;
			distribution[98] = 4;
			distribution[99] = 8;
		}
		if (i == 3) {
			//scenario 3
				// most flights in the middle with some at the beginning and the end of the available slots
			distribution = new double[100];
			distribution = fillArraySlots(distribution, 0, 6, 0.2);
			distribution = fillArraySlots(distribution, 6, 21, 0);
			distribution = fillArraySlots(distribution, 21, 26, 0.2);
			distribution = fillArraySlots(distribution, 26, 41, 0);
			distribution = fillArraySlots(distribution, 41, 46, 0.6);
			distribution[46] = 0.8;
			distribution[47] = 0.8;
			distribution = fillArraySlots(distribution, 47, 53, 1);
			distribution[53] = 0.8;
			distribution[54] = 0.8;
			distribution = fillArraySlots(distribution, 54, 61, 0.6);
			distribution = fillArraySlots(distribution, 61, 76, 0);
			distribution = fillArraySlots(distribution, 76, 81, 0.2);
			distribution = fillArraySlots(distribution, 81, 96, 0);
			distribution = fillArraySlots(distribution, 96, 100, 0.2);
		}
		if (i == 4) {
			// scenario 4
				// most flights in the middle with some between 0-10/20-30 and 71-81/91-101
			distribution = new double[100];
			distribution = fillArraySlots(distribution, 0, 10, 1);
			distribution = fillArraySlots(distribution, 10, 20, 0);
			distribution = fillArraySlots(distribution, 20, 37, 1);
			distribution = fillArraySlots(distribution, 37, 40, 0);
			distribution = fillArraySlots(distribution, 40, 48, 2);
			distribution = fillArraySlots(distribution, 48, 53, 3);
			distribution = fillArraySlots(distribution, 53, 61, 2);
			distribution = fillArraySlots(distribution, 61, 71, 0);
			distribution = fillArraySlots(distribution, 64, 81, 1);
			distribution = fillArraySlots(distribution, 81, 91, 0);
			distribution = fillArraySlots(distribution, 91, 100, 1);
		}

		if (/*logger.isInfoEnabled() || */logger.isDebugEnabled()) {
			for (int l = 0; l < distribution.length; l++) {
				logger.debug("openFlights[" + l + "]: " + distribution[l]);
			}
		}
		return distribution;
	}

	
	// helper methods for getcurrentOpenFlights
	/**
	 * Return first highest number of the given array
	 * @param array given array
	 * @return first highest number of the given array
	 */
	private static int getHighestPosOfArray(int[] array) {
		int max = Integer.MIN_VALUE;
		int pos = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] > max) {
				max = array[i];
				pos = i;
			}
		}
		return pos;
	}
	/**
	 * Return sum of the array elements (int[] array)
	 * @param array given array
	 * @return sum of the array element values
	 */
	private static int sumOfArray(int[] array) {
		int sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum = sum + array[i];
		}
		return sum;
	}
	// (also helper for getflightMap)
	/**
	 * Helper method for getFlightMap as well. Return sum of the array elements (double[] array)
	 * @param array given array
	 * @return sum of the array element values
	 */
	private static double sumOfArray(double[] array) {
		double sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum = sum + array[i];
		}
		return sum;
	}
	/**
	 * Fill array slots from beginIndex (including) to endIndex (excluding) with value.
	 * @param array given array, to be filled with values
	 * @param beginIndex first position from which the values have to be inserted
	 * @param endIndex first position from which the values do not have to be inserted
	 * @param value value to insert into given positions
	 * @return array with inserted values
	 */
	private static double[] fillArraySlots(double[] array, int beginIndex, int endIndex, double value) {
		for (int i = beginIndex; i < endIndex; i++) {
			array[i] = value;
		}
		return array;
	}
	/**
	 * Fill every 'everyXthValue' array slot from beginIndex (including) to endIndex (excluding) with value.
	 * @param array given array, to be filled with values
	 * @param beginIndex first position from which the values have to be inserted
	 * @param endIndex first position from which the values do not have to be inserted
	 * @param value value to insert into given positions
	 * @param everyXthValue how often the value has to be inserted
	 * @return array with inserted values
	 */
	private static double[] fillArraySlots(double[] array, int beginIndex, int endIndex, double value, int everyXthValue) {
		for (int i = beginIndex; i < endIndex; i++) {
			if ( i % everyXthValue == 0) {
				array[i] = value;
			}
			array[i] = 0;
		}
		return array;
	}
	
	// helper method for generateMargin
	/**
	 * Generates flight map, stating how many flights have TimeWished in the particular slot
	 * @return flight map ([0, 2, 0] -> all 2 flights in second slot)
	 */
	/**
	 * Helper method for generateMargin. Generates flight map, stating how many flights have TimeWished in the particular slot.
	 * @param flightCount amount of flights
	 * @param distribution distribution of flight map
	 * @param slots list of given slots
	 * @param distributionSetting setting for distribution
	 * @return flight map ([0, 2, 0] -> all 2 flights in second slot)
	 */
	private static int[] getFlightMap(int flightCount, double[] distribution, List<Slot> slots, int distributionSetting) {
		int slotCount = slots.size(); // same as flightCount
		if (slotCount <= 0 || flightCount <= 0 || distribution == null) {
			return new int[0];
		}
		if (slotCount == 1) {
			int[] flightMap = {1};
			return flightMap;
		}
		
		// adapt distribution to current flightCount and insert values according to distribution
		double[] adaptedDistribution = new double[flightCount];
		for (int i = 0; i < flightCount; i++) {
			int currentPerc = getCurrentPerc(flightCount, i);
			adaptedDistribution[i] = distribution[currentPerc];// * 100;
			logger.debug("current Perc:" + currentPerc + " | i: " + i + " | adaptedDistribution[i]: " + adaptedDistribution[i]);
		}
		
		double sumAdaptedDistribution = sumOfArray(adaptedDistribution);
		double factorForFlightMap = ((double) flightCount) / sumAdaptedDistribution;
		logger.debug("factor for flightMap: " + factorForFlightMap + " | flightCount: " + flightCount + " | sum: " + sumAdaptedDistribution);
		int[] flightMap = new int[flightCount];
		for (int i = 0; i < flightCount; i++) {
			flightMap[i] = (int) (adaptedDistribution[i] * factorForFlightMap);
		}
		// repair section (as the algorithm may have missed some flights or falsely assigned some flights
		if (sumOfArray(flightMap) != flightCount) {
			flightMap = repairFlightMap(flightMap, distributionSetting, flightCount);
			// assign remaining missing flights if repairFlightMap missed some flights or was
			// unable to repair
			int highestPosPercentageDistr = getHighestPosOfArray(flightMap);
			int missingFlights = flightCount - sumOfArray(flightMap);
			flightMap[highestPosPercentageDistr] = flightMap[highestPosPercentageDistr] + missingFlights;
		} 
		if (/*logger.isInfoEnabled() || */logger.isDebugEnabled()) {
			for (int l = 0; l < flightMap.length; l++) {
				logger.debug("flightMap[" + l + "]: " + flightMap[l]);
			}
			logger.debug("Sum of all flights in flightMap: " + sumOfArray(flightMap));
		}
		return flightMap;
	}
	// can be used to assign flightDifference (missing flights) more evenly and not just at one point
	/**
	 * Repairs a flight map (used when flightCount != sumOfArray(flightMap))
	 * @param flightMap given flightMap, with set flights (usually missing some flights)
	 * @param distributionSetting setting of flight distribution, necessary to determine if 
	 * 		the highest values are in the middle or somewhere else
	 * @param flightCount count of flights
	 * @return repaired flightMap
	 */
	private static int[] repairFlightMap(int[] flightMap, int distributionSetting, int flightCount) {
		int flightDifference = flightCount - sumOfArray(flightMap);
		if (flightCount < 3) {
			// if only 0, 1 or 2 flights, then assign all missing flights (or subtract all flights, which are too many) 
			// to first highest value in the array
			int highestPosPercentageDistr = getHighestPosOfArray(flightMap);
			flightMap[highestPosPercentageDistr] = flightMap[highestPosPercentageDistr] + flightDifference;
			return flightMap;
		}
		// if the highest value is in the middle of the flightMap
			// for setting 1, 3 and 4 (where the highest value is somewhere in the middle)
		if (distributionSetting == 1 || distributionSetting == 3 || distributionSetting == 4) {
			int highestPosValue = getHighestPosOfArray(flightMap);
			int currentPos1 = highestPosValue;
			int currentPos2 = highestPosValue;
			// go with currentPos1 from middle point in array to lowest point in array
			//		and add a flight every defined step if flights are missing
			// go with currentPos2 from middle point in array to highest point in array
			//		and add a flight every defined step if flights are missing
			while (currentPos1 >= 0 && currentPos2 < flightMap.length &&
					flightDifference > 0 && (int) (flightMap.length/10) >= 1) {
				if (flightMap[currentPos1] == 0) {
					flightMap[currentPos1] = 1;
					flightDifference = flightDifference - 1;
				}
				if (flightMap[currentPos2] == 0) {
					flightMap[currentPos2] = 1;
					flightDifference = flightDifference - 1;
				}
				currentPos1 = currentPos1 - (int) (flightMap.length/10);
				currentPos2 = currentPos2 + (int) (flightMap.length/10);	
			}
			
			// reset and fill other slots, if slots are still open without flights
			// go with currentPos1 from middle point in array to lowest point in array
			//		and add a flight every defined step if flights are still missing
			// go with currentPos2 from middle point in array to highest point in array
			//		and add a flight every defined step if flights are still missing
			currentPos1 = highestPosValue;
			currentPos2 = highestPosValue;
			while (currentPos1 >= 0 && currentPos2 < flightMap.length && flightDifference > 0) {
				if (flightMap[currentPos1] == 0) {
					flightMap[currentPos1] = 1;
					flightDifference = flightDifference - 1;
				}
				if (flightMap[currentPos2] == 0) {
					flightMap[currentPos2] = 1;
					flightDifference = flightDifference - 1;
				}
				currentPos1 = currentPos1 - 1;
				currentPos2 = currentPos2 + 1;	
			}
			return flightMap;
		}
		// if the highest value is at the beginning and end of the array
			// for setting 2 (where the highest value is not in the middle)
		if (distributionSetting == 2) {
			int currentPos1 = 0;
			int currentPos2 = flightMap.length - 1;
			// go with currentPos1 from lowest point in array to middle point in array
			//		and add a flight every defined step if flights are missing
			// go with currentPos2 from highest point in array to middle point in array
			//		and add a flight every defined step if flights are missing
			while(currentPos1 < flightMap.length && currentPos2 >= 0 && 
					flightDifference > 0 && (int) (flightMap.length/10) >= 1) {
				if (flightMap[currentPos1] == 0) {
					flightMap[currentPos1] = 1;
					flightDifference = flightDifference - 1;
				}
				if (flightMap[currentPos2] == 0) {
					flightMap[currentPos2] = 1;
					flightDifference = flightDifference - 1;
				}
				currentPos1 = currentPos1 + (int) (flightMap.length/10);
				currentPos2 = currentPos2 - (int) (flightMap.length/10);	
			}
			
			// reset and fill other slots
			// go with currentPos1 from lowest point in array to middle point in array
			//		and add a flight every single step if flights are still missing
			// go with currentPos2 from highest point in array to middle point in array
			//		and add a flight every single step if flights are still missing
			currentPos1 = 0;
			currentPos2 = flightMap.length - 1;
			while(currentPos1 < flightMap.length && currentPos2 >= 0 && flightDifference > 0) {
				if (flightMap[currentPos1] == 0) {
					flightMap[currentPos1] = 1;
					flightDifference = flightDifference - 1;
				}
				if (flightMap[currentPos2] == 0) {
					flightMap[currentPos2] = 1;
					flightDifference = flightDifference - 1;
				}
				currentPos1 = currentPos1 + 1;
				currentPos2 = currentPos2 - 1;	
			}
			return flightMap;
		}
		return flightMap;
	}

	// returns current percentage (i = 2, flightCount = 100 -> return 2%)
	/**
	 * Returns current percentage (i of flightCount), with 0/100 if i is smaller/larger than flightCount
	 * @param flightCount amount of flights
	 * @param i current position
	 * @return current percentage
	 */
	private static int getCurrentPerc(int flightCount, int i) {
		if (i < 0 || flightCount < 0) {
			return 0;
		}
		if (i > flightCount) {
			return 100;
		}
		int currentPerc = (int) (((double) i/(double) flightCount)*100);
		//if the current percentage is 0, 99, 100
		if (currentPerc >= 99) {
			return 99;
		} else if (currentPerc <= 0) {
			return 0;
		} else {
			// for other percentages
			return currentPerc;
		}
	}

	/**
	 * Helper method for generateMargins, return current priority (of priority array at location i)
	 * and allows for random values between two values
	 * @param priority settings for priority
	 * @param i current position
	 * @param size size
	 * @return current priority (randomized between defined lower and upper value)
	 */
	private static double getCurrentPriority(double[][] priority, int i, int size) {
		double currentPriority = 1.0;
		for (int k = 0; k < priority.length; k++) {
			double currentLocation = ((double)(i)/(double)(size)) * 100; // 20 -> at 20%
			if (currentLocation < priority[k][1] && currentLocation >= priority[k][0]) {
				Random r = new Random();
				OptionalDouble od = r.doubles(priority[k][2], priority[k][3] + 1E-10).findFirst();
				try {
					currentPriority = od.getAsDouble();
				} catch (Exception e) {
					// if no value is present use lower value
					currentPriority = priority[k][2];
				}
				logger.debug("current priority is: " + currentPriority);
				return currentPriority;
			}
			
		}
		// default value;
		return currentPriority;
	}

	/**
	 * Generates a flight, calls .generateWeightMap to get weight map for Flight object
	 * @param flightId current flightId
	 * @param scheduledTime current scheduledTime
	 * @param me MarginEntry object
	 * @param slots list of given slots
	 * @param optE OptimizationEntry object
	 * @return Flight object
	 */
	private static Flight generateFlight(String flightId, LocalDateTime scheduledTime, MarginEntry me, 
			List<Slot> slots, OptimizationEntry optE) {
		int[] weightMap = FlightGenerator.generateWeightMap(me, slots, optE);
		
		Flight f = new Flight(flightId, scheduledTime.atZone(ZoneId.of("Europe/Vienna")).toInstant(), weightMap);
		
		return f;
	}
	
	/**
	 * Generates a MarginEntry object
	 * @param flightId current flightId
	 * @param scheduledTime current scheduled time
	 * @param marginWindowWidth duration between TimeNotBefore and TimeNotAfter (in seconds)
	 * @param timeWished current wished time
	 * @param priority priority of this MarginEntry object
	 * @param slots given list of slots
	 * @param optE OptimizationEntry object
	 * @return generated MarginEntry object
	 */
	private static MarginEntry generateMargin(String flightId, LocalDateTime scheduledTime, int marginWindowWidth, 
			LocalDateTime timeWished, double priority, List<Slot> slots, OptimizationEntry optE) {
		LocalDateTime timeNotBefore = timeWished.minusSeconds(marginWindowWidth/2);
		LocalDateTime timeNotAfter = timeWished.plusSeconds(marginWindowWidth/2);
			
		MarginEntry me = new MarginEntry(flightId, scheduledTime, timeNotBefore, timeWished, 
				timeNotAfter, priority);
		
		return me;
	}
	
	/**
	 * Generate slots with length slotLengthSec, starting at slotStartTime to get slotCount slots.
	 * @param slotCount
	 * @param slotStartTime
	 * @param slotLengthSec
	 * @return generated slots
	 */
	private static SlotInfo[] generateSlots(int slotCount, Instant slotStartTime, int slotLengthSec) {
		if (slotCount < 0 || slotLengthSec < 0) {
			return new SlotInfo[0];
		}
		SlotInfo[] slots = new SlotInfo[slotCount];
		Instant currentSlot = slotStartTime;
		for (int i = 0; i < slotCount; i++) {
			slots[i] = new SlotInfo(currentSlot);
			currentSlot = currentSlot.plusSeconds(slotLengthSec);
		}
		return slots;
	}
	
	/**
	 * Converts array of SlotInfo-objects to list of Slot-objects.
	 * @param slots array of SlotInfo-objects
	 * @return converted list of Slot-objects
	 */
	private static List<Slot> convertSlotInfoToSlotList(SlotInfo[] slots){
		List<Slot> slotList = new LinkedList<Slot>();
		for (int i = 0; i < slots.length; i++) {
			// sequence numbers begin at 0
			slotList.add(new Slot((i+1), slots[i].getTime().atZone(ZoneId.of("Europe/Vienna")).toLocalDateTime()));
		}
		return slotList;
	}
	
	/**
	 * Generate configurations for Jenetics (currently 4 different configurations are supported)
	 * @param i amount of different configurations (only up to DEFINENUMBER, currently 4, are supported)
	 * @param maxTime maximum time per optimization session (in seconds)
	 * @return array of Jenetic configurations
	 */
	private static List<HashMap<String, Object>> generateJenConfigs(int maxTime) {
		List<HashMap<String, Object>> jenConfigs = new LinkedList<HashMap<String, Object>>();
		// TODO generate new settings
		// default settings
		for (int i = 0; i < 16; i++) {
			HashMap<String, Object> currentJenConfig = new HashMap<String, Object>();
			currentJenConfig.put("crossover", "PARTIALLY_MATCHED_CROSSOVER");
//			currentJenConfig.put("crossoverAlterProbability", 0.9);
			currentJenConfig.put("mutator", "SWAP_MUTATOR");
//			currentJenConfig.put("mutatorAlterProbability", 0.15);
			
			//TODO set default value correctly
			currentJenConfig.put("maximalPhenotypeAge", 80);
//			currentJenConfig.put("populationSize", 70);
			currentJenConfig.put("offspringFraction", 0.7);
			currentJenConfig.put("offspringSelector", "TOURNAMENT_SELECTOR");
			currentJenConfig.put("survivorsSelector", "TOURNAMENT_SELECTOR");
			
			HashMap<String, Object> terminationConditions = new HashMap<String, Object>();
			terminationConditions.put("BY_EXECUTION_TIME", maxTime);
			currentJenConfig.put("terminationConditions", terminationConditions);
			jenConfigs.add(currentJenConfig);
	
			// several (DEFINENUMBER, currently 16) different settings/configurations for Jenetics
			if (i == 0) {
				currentJenConfig.put("offspringSelectorParameter", 50);
				currentJenConfig.put("survivorsSelectorParameter", 50);
				currentJenConfig.put("populationSize", 500);
				currentJenConfig.put("crossoverAlterProbability", 0.35);
				currentJenConfig.put("mutatorAlterProbability", 0.15);
			}
			if (i == 1) {
				currentJenConfig.put("offspringSelectorParameter", 50);
				currentJenConfig.put("survivorsSelectorParameter", 50);
				currentJenConfig.put("populationSize", 500);
				currentJenConfig.put("crossoverAlterProbability", 0.9);
				currentJenConfig.put("mutatorAlterProbability", 0.15);
			}
			if (i == 2) {
				currentJenConfig.put("offspringSelectorParameter", 50);
				currentJenConfig.put("survivorsSelectorParameter", 50);
				currentJenConfig.put("populationSize", 500);
				currentJenConfig.put("crossoverAlterProbability", 0.35);
				currentJenConfig.put("mutatorAlterProbability", 0.6);
			}
			if (i == 3) {
				currentJenConfig.put("offspringSelectorParameter", 50);
				currentJenConfig.put("survivorsSelectorParameter", 50);
				currentJenConfig.put("populationSize", 500);
				currentJenConfig.put("crossoverAlterProbability", 0.9);
				currentJenConfig.put("mutatorAlterProbability", 0.6);
			}
			if (i == 4) {
				currentJenConfig.put("offspringSelectorParameter", 10);
				currentJenConfig.put("survivorsSelectorParameter", 10);
				currentJenConfig.put("populationSize", 500);
				currentJenConfig.put("crossoverAlterProbability", 0.35);
				currentJenConfig.put("mutatorAlterProbability", 0.15);
			}
			if (i == 5) {
				currentJenConfig.put("offspringSelectorParameter", 10);
				currentJenConfig.put("survivorsSelectorParameter", 10);
				currentJenConfig.put("populationSize", 500);
				currentJenConfig.put("crossoverAlterProbability", 0.9);
				currentJenConfig.put("mutatorAlterProbability", 0.15);
			}
			if (i == 6) {
				currentJenConfig.put("offspringSelectorParameter", 10);
				currentJenConfig.put("survivorsSelectorParameter", 10);
				currentJenConfig.put("populationSize", 500);
				currentJenConfig.put("crossoverAlterProbability", 0.35);
				currentJenConfig.put("mutatorAlterProbability", 0.6);
			}
			if (i == 7) {
				currentJenConfig.put("offspringSelectorParameter", 10);
				currentJenConfig.put("survivorsSelectorParameter", 10);
				currentJenConfig.put("populationSize", 500);
				currentJenConfig.put("crossoverAlterProbability", 0.9);
				currentJenConfig.put("mutatorAlterProbability", 0.6);
			}
			if (i == 8) {
				currentJenConfig.put("offspringSelectorParameter", 3);
				currentJenConfig.put("survivorsSelectorParameter", 3);
				currentJenConfig.put("populationSize", 70);
				currentJenConfig.put("crossoverAlterProbability", 0.35);
				currentJenConfig.put("mutatorAlterProbability", 0.15);
			}
			if (i == 9) {
				currentJenConfig.put("offspringSelectorParameter", 3);
				currentJenConfig.put("survivorsSelectorParameter", 3);
				currentJenConfig.put("populationSize", 70);
				currentJenConfig.put("crossoverAlterProbability", 0.9);
				currentJenConfig.put("mutatorAlterProbability", 0.15);
			}
			if (i == 10) {
				currentJenConfig.put("offspringSelectorParameter", 3);
				currentJenConfig.put("survivorsSelectorParameter", 3);
				currentJenConfig.put("populationSize", 70);
				currentJenConfig.put("crossoverAlterProbability", 0.35);
				currentJenConfig.put("mutatorAlterProbability", 0.6);
			}
			if (i == 11) {
				currentJenConfig.put("offspringSelectorParameter", 3);
				currentJenConfig.put("survivorsSelectorParameter", 3);
				currentJenConfig.put("populationSize", 70);
				currentJenConfig.put("crossoverAlterProbability", 0.9);
				currentJenConfig.put("mutatorAlterProbability", 0.6);
			}
			if (i == 12) {
				currentJenConfig.put("offspringSelectorParameter", 10);
				currentJenConfig.put("survivorsSelectorParameter", 10);
				currentJenConfig.put("populationSize", 70);
				currentJenConfig.put("crossoverAlterProbability", 0.35);
				currentJenConfig.put("mutatorAlterProbability", 0.15);
			}
			if (i == 13) {
				currentJenConfig.put("offspringSelectorParameter", 10);
				currentJenConfig.put("survivorsSelectorParameter", 10);
				currentJenConfig.put("populationSize", 70);
				currentJenConfig.put("crossoverAlterProbability", 0.9);
				currentJenConfig.put("mutatorAlterProbability", 0.15);
			}
			if (i == 14) {
				currentJenConfig.put("offspringSelectorParameter", 10);
				currentJenConfig.put("survivorsSelectorParameter", 10);
				currentJenConfig.put("populationSize", 70);
				currentJenConfig.put("crossoverAlterProbability", 0.35);
				currentJenConfig.put("mutatorAlterProbability", 0.6);
			}
			if (i == 15) {
				currentJenConfig.put("offspringSelectorParameter", 10);
				currentJenConfig.put("survivorsSelectorParameter", 10);
				currentJenConfig.put("populationSize", 70);
				currentJenConfig.put("crossoverAlterProbability", 0.9);
				currentJenConfig.put("mutatorAlterProbability", 0.6);
			}
		}
		return jenConfigs;
	}
	
	/**
	 * Generate configurations for OptaPlanner in array slots i to j (currently
	 * 8 different OptaPlanner configurations are supported)
	 * @param i beginning position of array
	 * @param j end position of array
	 * @param maxTime maximum time per optimization session (in seconds)
	 * @return array of OptaPlanner configurations
	 */
	private static List<HashMap<String, Object>> generateOptConfigs(int maxTime) {
		List<HashMap<String, Object>> optConfigs = new LinkedList<HashMap<String, Object>>();
		

		// TODO work with new parameters from Heuristic Optimizer and adjust this method accordingly
		// default settings
		for (int i = 0; i < 8; i++) {
			HashMap<String, Object> currentOptConfig = new HashMap<String, Object>();

			
			// several (j-i) different settings/configurations for OptaPlanner
			// first settings use default values used by OptaPlanner
			// setting i+0 (Hill Climbing)
			if (i == 0) {
				currentOptConfig.put("configurationName", "HILL_CLIMBING");
				currentOptConfig.put("secondsSpentLimit", maxTime);
				// local search type: HILL CLIMBING
				optConfigs.add(currentOptConfig);
			}
			// setting i+1 (Tabu Search)
			if (i == 1) {
				currentOptConfig.put("configurationName", "TABU_SEARCH");
				currentOptConfig.put("secondsSpentLimit", maxTime);
				// local search type: TABU SEARCH
				optConfigs.add(currentOptConfig);
			}
			// setting i+2 (Simulated Annealing)
			if (i == 2) {
				currentOptConfig.put("configurationName", "SIMULATED_ANNEALING");
				currentOptConfig.put("secondsSpentLimit", maxTime);
				// Simulated Annealing StartTemp: 0hard/500soft
				optConfigs.add(currentOptConfig);
			}
			
			// setting i+3 (Late Acceptance)
			if (i == 3) {			
				currentOptConfig.put("configurationName", "LATE_ACCEPTANCE");
				currentOptConfig.put("secondsSpentLimit", maxTime);
				// Late Acceptance Size: 50
				// AcceptedCountLimit: 4
				optConfigs.add(currentOptConfig);
			}
			
			// setting i+4 (Great Deluge)
			if (i == 4) {		
				currentOptConfig.put("configurationName", "GREAT_DELUGE");
				currentOptConfig.put("secondsSpentLimit", maxTime);
				// Great Deluge Water Level Increasig Score 0hard/100soft (or perhaps 0hard/5000000soft)
				// AcceptedCountLimit: 1
				optConfigs.add(currentOptConfig);
			}
			
			// setting i+5 (Step Counting Hill Climbing)
			if (i == 5) {	
				currentOptConfig.put("configurationName", "STEP_COUNTING_HILL_CLIMBING");
				currentOptConfig.put("secondsSpentLimit", maxTime);
				// Step Counting Hill Climbing size: 20
				// AcceptedCountLimit: 1
				optConfigs.add(currentOptConfig);
			}
			
			// setting i+6 (Strategic Oscillation)
			if (i == 6) {		
				currentOptConfig.put("configurationName", "STRATEGIC_OSCILLATION");
				currentOptConfig.put("secondsSpentLimit", maxTime);
				// Tabu Search: entityTabuRatio 0.04
				// AcceptedCountLimit: 1000
				// finalistPodiumType: STRATEGIC_OSCILLATION_BY_LEVEL_ON_BEST_SCORE
				optConfigs.add(currentOptConfig);
			}
//			// setting i+7 // not enabled
//			if (i == 7) {
//				currentOptConfig.put("configurationName", "BRANCH_AND_BOUND");
//				currentOptConfig.put("secondsSpentLimit", maxTime);
//				optConfigs.add(currentOptConfig);
//			}
		}
		
		return optConfigs;
	}

	/**
	 * Check data inputs from TestDataConfigDTO. If invalid data inputs are found, use default values.
	 * @param tdgConfig TestDataConfigDTO test data configuration
	 * @return cleaned tdgConfig
	 */
	private static TestDataConfigDTO checkInputs(TestDataConfigDTO tdgConfig) {
		TestDataConfigDTO tdgConfigReserve = new TestDataConfigDTO();
		if (tdgConfig == null) {
			tdgConfig = tdgConfigReserve;
		}
		if (tdgConfig.getFlightCount() < 1) {
			tdgConfig.setFlightCount(1);
			logger.info("Flight count must be higher than zero. Set to default (= 1).");
		}
		if (tdgConfig.getSlotCount() < 1) {
			tdgConfig.setSlotCount(1);
		}
		if (tdgConfig.getFlightCount() > tdgConfig.getSlotCount()) {
			tdgConfig.setFlightCount(tdgConfig.getSlotCount());
		}
		if (tdgConfig.getFlightCount() < tdgConfig.getSlotCount()) {
			tdgConfig.setFlightCount(tdgConfig.getSlotCount());
		}
		if (tdgConfig.getFlightPrefix() == null) {
			tdgConfig.setFlightPrefix("F");
		}
		if (tdgConfig.getSlotStartTime() == null) {
			tdgConfig.setSlotStartTime(Instant.now());
		}
		if (tdgConfig.getSlotLengthSec() < 1) {
			tdgConfig.setSlotLengthSec(60);
		}
		if (tdgConfig.getMarginWindowLength() < 1) {
			tdgConfig.setMarginWindowLength(600);
		}
		if (tdgConfig.getDistributionSetting() < 0) {
			tdgConfig.setDistributionSetting(0);
			//TODO allow no values below or above count of presets
		}
		if (tdgConfig.getOptimizationFramework() == null) {
			tdgConfig.setOptimizationFramework("OPTAPLANNER");
		}
		// no additional checks for prioritySettings
		if (tdgConfig.getPrioritySettings().length == 0) {
			double[][] prioritySetting = {{0,100,0.9,1.1}};
			tdgConfig.setPrioritySettings(prioritySetting);
		}
		if (tdgConfig.getMinValue() > tdgConfig.getMaxValue()) {
			int oldMinValue = tdgConfig.getMinValue();
			tdgConfig.setMinValue(tdgConfig.getMaxValue());
			tdgConfig.setMaxValue(oldMinValue);
		}
		if (tdgConfig.getMaxTime() < 1) {
			tdgConfig.setMaxTime(60);
		}
		if (logger.isInfoEnabled() || logger.isDebugEnabled()) {
			logger.info("Test Data Generator Configuration:");
			logger.info("\t flight count: " + tdgConfig.getFlightCount());
			logger.info("\t slot count: " + tdgConfig.getSlotCount());
			logger.info("\t flight prefix: " + tdgConfig.getFlightPrefix());
			logger.info("\t slot start time: " + tdgConfig.getSlotStartTime().atZone(ZoneId.of("Europe/Vienna")).toLocalDateTime());
			logger.info("\t slot length: " + tdgConfig.getSlotLengthSec());
			logger.info("\t margin window length (in seconds): " + tdgConfig.getMarginWindowLength());
			logger.info("\t distribution setting (presets): " + tdgConfig.getDistributionSetting());
			logger.info("\t optimization framework (may be overwritten later): " + tdgConfig.getOptimizationFramework());
			logger.info("\t priority settings: " + prioritySettingsToString(tdgConfig.getPrioritySettings()));
			logger.info("\t (weight map) min value: " + tdgConfig.getMinValue() + " | max value: " + tdgConfig.getMaxValue());
			logger.info("\t max time (for optimization sessions): " + tdgConfig.getMaxTime());
		}
		return tdgConfig;
	}
	/**
	 * Returns the prioritySetting array similar to [[0.0,20.0,0.5,1.0],[20.0,80.0,1.0,2.0],[80.0,100.0,5.0,10.0]]
	 * @param ds prioritySetting
	 * @return String of prioritySetting array
	 */
	private static String prioritySettingsToString(double[][] ds) {
		String output = "[";
		for (int i = 0; i < ds.length; i++) {
			output = output + "[";
			for (int j = 0; j < ds[i].length; j++) {
				output = output + ds[i][j] + ",";
			}
			output = output.substring(0, output.length()-1) + "],";
		}
		output = output.substring(0, output.length()-1) + "]";
		return output;
	}
}
