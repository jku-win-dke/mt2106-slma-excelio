package at.jku.dke.slotmachine.data.excel;

import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExcelInputReader {
    private static final Logger logger = LogManager.getLogger();

    /**
     * Read Margins from given Excel input file and return them as a list of margins.
     *
     * @param fileName location and name of given input file
     * @return a list of margins
     */
    public static List<MarginEntry> readMarginsFromFile(String fileName) {
        List<MarginEntry> entryList = new LinkedList<MarginEntry>();

        try (InputStream input = new FileInputStream(fileName)) {
            logger.info("Reading sheet 'Flights' from Excel file '" + fileName + "'.");
            logger.info("Reading margins from the file.");

            Workbook wb = WorkbookFactory.create(input);

            Sheet sheetFlights = wb.getSheet("Flights");

            int rowNum = 1; //information is in row 1 (second row) and below
            boolean eof = false;

            while (!eof) {
                Row row = sheetFlights.getRow(rowNum);

                if (row != null && row.getCell(0) != null){
                    Cell flightId = row.getCell(0);
                    Cell scheduledTime = row.getCell(1);
                    Cell timeNotBefore = row.getCell(2);
                    Cell timeWished = row.getCell(3);
                    Cell timeNotAfter = row.getCell(4);
                    Cell priority = row.getCell(5);

                    MarginEntry entry = new MarginEntry(
                        flightId.getStringCellValue(),
                        // set seconds and nanoseconds to zero (Excel sometimes adds small values to dates)
                        scheduledTime.getLocalDateTimeCellValue().withSecond(0).withNano(0),
                        timeNotBefore.getLocalDateTimeCellValue().withSecond(0).withNano(0),
                        timeWished.getLocalDateTimeCellValue().withSecond(0).withNano(0),
                        timeNotAfter.getLocalDateTimeCellValue().withSecond(0).withNano(0),
                        priority.getNumericCellValue()
                    );

                    entryList.add(entry);

                    rowNum++;
                } else {
                    eof = true;
                }
            }
        } catch (IOException e) {
            logger.error("Could not read Excel file.", e);
        }

        return entryList;
    }

    /**
     * Read Optimizations from excel source file (without weight maps).
     * 
     * @param fileName file name of excel source file
     * @return list of optimizations
     */
    public static List<OptimizationEntry> readOptimizationsFromFile(String fileName) {
        List<OptimizationEntry> entryList = new LinkedList<OptimizationEntry>();

        try (InputStream input = new FileInputStream(fileName)) {
            logger.info("Reading sheet 'Optimizations' from Excel file '" + fileName + "'.");
            logger.info("Reading optimizations from the excel source file.");

            Workbook wb = WorkbookFactory.create(input);

            Sheet sheetOptimizations = wb.getSheet("Optimizations");

            int rowNum = 1; //information is in row 1 (second row) and below
            boolean eof = false;

            while (!eof) {
                Row row = sheetOptimizations.getRow(rowNum);

                if (row != null ) {
                    Cell optId = row.getCell(0);
                    Cell startTime = row.getCell(1);
                    Cell endTime = row.getCell(2);
                    Cell interval = row.getCell(3);
                    Cell framework = row.getCell(4);
                    String frameworkString = "null";
                    if (framework != null) {
                    	frameworkString = framework.getStringCellValue();
                    }
                    Cell minValue = row.getCell(5);
                    Cell maxValue = row.getCell(6);
                    Cell dropValue = row.getCell(7);
                    
                    OptimizationEntry entry = new OptimizationEntry(
                        optId.getStringCellValue(),
                        startTime.getLocalDateTimeCellValue().withSecond(0).withNano(0),
                        endTime.getLocalDateTimeCellValue().withSecond(0).withNano(0),
                        (int) interval.getNumericCellValue(),
                        frameworkString,
                        minValue.getNumericCellValue(),
                        maxValue.getNumericCellValue(),
                        dropValue.getNumericCellValue()
                    );

                    logger.info("OptimizationEntry: " + entry.getOptId() + "\n \t starttime: " + 
                    		entry.getStartTime() + " \t endtime: " + entry.getEndTime() + 
                    		"\n \t interval: " + entry.getInterval() + " \t framework: " + entry.getOptimizationFramework() +
                    		"\n \t minValue: " + entry.getMinValue() + " \t maxValue: " + entry.getMaxValue());
                    
                    entryList.add(entry);

                    rowNum++;
                } else {
                    eof = true;
                }
            }
        } catch (IOException e) {
            logger.error("Could not read Excel file.", e);
        }

        return entryList;
    }

    /**
     * Check if the file at given fileName is excel source or excel target file. The 
     * checked difference is if the target file contains a weight map at the second sheet
     * which should be the sheet of the first flight.
     * 
     * @param fileName file name of the excel file
     * @return true if file is excel target file, otherwise false
     */
	public static boolean isTargetFile(String fileName) {
		// return true if file is source file or return false if file is target file 
		// (target file contains 'weight'-Maps and therefore first row second sheet is 'Slot | Weight' -> false)
		// default is to return true
        try (InputStream input = new FileInputStream(fileName)) {
            logger.info("Reading sheet 'Optimizations' from Excel file '" + fileName + "' to check if file is Excel Source or Target file.");

            Workbook wb = WorkbookFactory.create(input);
            
            try {
	            Sheet weightMap = wb.getSheetAt(1);
	            if (weightMap.getRow(0).getCell(0).getStringCellValue().equals("Slot") && 
	            		weightMap.getRow(0).getCell(1).getStringCellValue().equals("Weight")) {
	            	return true;
	            }
	            return false;
            } catch (IllegalArgumentException e) {
            	return false;
            }
		
        } catch (IOException e) {
            logger.error("Could not read Excel file.", e);
        }
		return false;
	}

    /**
     * Read Optimization from excel target file (with weight maps).
     * 
     * @param fileName file name of excel target file
     * @return optimization
     */
	public static OptimizationEntry readOptimizationFromTargetFile(String fileName) {
		OptimizationEntry entry = null;
        try (InputStream input = new FileInputStream(fileName)) {
            logger.info("Reading sheet 'Optimizations' from Excel file '" + fileName + "'.");
            logger.info("Reading optimization from the excel target file.");

            Workbook wb = WorkbookFactory.create(input);

            Sheet sheetOptimizations = wb.getSheet("Flights");

            Row row = sheetOptimizations.getRow(0);

            Cell optId = row.getCell(8);
            
            row = sheetOptimizations.getRow(2);
            Cell startTime = row.getCell(8);
            
            row = sheetOptimizations.getRow(3);
            Cell endTime = row.getCell(8);
            
            row = sheetOptimizations.getRow(4);
            Cell interval = row.getCell(8);
            
            row = sheetOptimizations.getRow(1);
            Cell framework = row.getCell(8);
            String frameworkString = "null";
            if (framework != null) {
            	frameworkString = framework.getStringCellValue();
            }
            
            row = sheetOptimizations.getRow(5);
            Cell minValue = row.getCell(8);
            
            row = sheetOptimizations.getRow(6);
            Cell maxValue = row.getCell(8);
            
            row = sheetOptimizations.getRow(7);
            Cell dropValue = row.getCell(8);
            
            entry = new OptimizationEntry(
                optId.getStringCellValue(),
                startTime.getLocalDateTimeCellValue().withSecond(0).withNano(0),
                endTime.getLocalDateTimeCellValue().withSecond(0).withNano(0),
                (int) interval.getNumericCellValue(),
                frameworkString,
                minValue.getNumericCellValue(),
                maxValue.getNumericCellValue(),
                dropValue.getNumericCellValue()
            );

            logger.info("OptimizationEntry: " + entry.getOptId() + "\n \t starttime: " + 
            		entry.getStartTime() + " \t endtime: " + entry.getEndTime() + 
            		"\n \t interval: " + entry.getInterval() + " \t framework: " + entry.getOptimizationFramework() +
            		"\n \t minValue: " + entry.getMinValue() + " \t maxValue: " + entry.getMaxValue());

        } catch (IOException e) {
            logger.error("Could not read Excel file.", e);
        }

        return entry;
	}
	
	/**
	 * Converts excel file (from fileName) which has to be in format as the files are, when weight maps
	 * have been written to them (as the output of writeWeightsToFile is).
	 * @param fileName Excel file name
	 * @return OptimizationSessionEntry to be used for creating OptimizationDTO (SlotMachine-Optimizer)
	 */
	public static OptimizationSessionEntry convertToOptimizationSessionEntry(String fileName) {
        try (InputStream input = new FileInputStream(fileName)) {
            logger.info("Reading from Excel file '" + fileName + "'.");
            logger.info("Read data from file to create OptimizationSessionEntry object from excel target file.");

            Workbook wb = WorkbookFactory.create(input);
            
            String optId = wb.getSheet("Flights").getRow(0).getCell(8).getStringCellValue();
            String framework = null;
            if (wb.getSheet("Flights").getRow(1) != null && wb.getSheet("Flights").getRow(1).getCell(8) != null &&
            		wb.getSheet("Flights").getRow(1).getCell(8).getStringCellValue() != null) {
            	framework = wb.getSheet("Flights").getRow(1).getCell(8).getStringCellValue();
            }
            
            // get amount of flights
			boolean eof = false;
			List<String> flightIdOrder = new LinkedList<String>();
			int pos = 0;
			do {
				pos++;
				if(wb.getSheet("Flights").getRow(pos) == null) {
					eof = true;
				} else {
					Row currentRow = wb.getSheet("Flights").getRow(pos);
	
					if(currentRow.getCell(0) == null || currentRow.getCell(0).getStringCellValue().equals("")) {
						eof = true;
					} else {
						String currentCell = currentRow.getCell(0).getStringCellValue();
						flightIdOrder.add(currentCell);
					}
				}
			} while (!eof);
			
			// get SlotInfo[] (list of slots with the time), with the help of the weight map of the first flight
			eof = false;
			List<LocalDateTime> slots = new LinkedList<LocalDateTime>();
			pos = 0;
			do {
				pos++;
				if(wb.getSheet(flightIdOrder.get(0)).getRow(pos) == null) {
					eof = true;
				} else {
					Row currentRow = wb.getSheet(flightIdOrder.get(0)).getRow(pos);
	
					if(currentRow.getCell(0) == null) {
						eof = true;
					} else {
						LocalDateTime currentCell = currentRow.getCell(0).getLocalDateTimeCellValue();
						slots.add(currentCell);
					}
				}
			} while (!eof);
			SlotInfo[] slotInfos = new SlotInfo[slots.size()];
			int i = 0;
			for (LocalDateTime ldt: slots) {
				slotInfos[i]= new SlotInfo(ldt.atZone(ZoneId.of("Europe/Vienna")).toInstant());
				i++;
			}
			
			// prepare flight-list (array to use for json later)
			Flight[] flights = new Flight[flightIdOrder.size()];
			// as no initial flight sequence is stored in the excel file the order is 
			// assumed as given by the rows (row 1 before row 2, ...)
			String[] initialFlightSequence = new String[flightIdOrder.size()];
			i = 0;
			for (String s: flightIdOrder) {
				// initial flight sequence is built
				initialFlightSequence[i] = s;
	
				// flight-objects are created
				String flightId = s;
				Instant scheduledTime = wb.getSheet("Flights")
						.getRow(i+1)
						.getCell(1)
						.getLocalDateTimeCellValue()
						.atZone(ZoneId.of("Europe/Vienna"))
						.toInstant();
				int[] weightMap = new int[slotInfos.length];
				for (int j = 0; j < weightMap.length; j++) {
					weightMap[j] = (int) wb.getSheet(flightId).getRow(j+1).getCell(1).getNumericCellValue();
				}
				flights[i] = new Flight(flightId, scheduledTime, weightMap);
				i++;
			}
			
            if (framework.equals("null")) {
            	framework = null;
            }
			
            logger.debug("OptimizationSessionEntry: optId: " + optId + " | initialFlightSequence length: " + initialFlightSequence.length + 
            		" | flightsArray length: " + flights.length + " | slots length: " + slotInfos.length + " | optimizationFramework: " + framework);
			    
			return new OptimizationSessionEntry(optId, initialFlightSequence, flights, slotInfos, framework);
			
        } catch (IOException e) {
            logger.error("Could not read Excel file.", e);
            return null;
        }
	}

	/**
	 * Converts excel file (from fileName) which has to be in format as the files are, when no weight maps
	 * have been written to them.
	 * @param excelSource excel source file
	 * @param flights  list of flights
	 * @param optId optimization id
	 * @return OptimizationSessionEntry to be used for creating OptimizationDTO (SlotMachine-Optimizer)
	 */
	public static OptimizationSessionEntry convertToOptimizationSessionEntryNoWeightMap(String excelSource, List<Flight> flights, String optId) {
		
        logger.info("Reading from Excel file '" + excelSource + "'.");
        logger.info("Read data from file to create OptimizationSessionEntry object from excel source file and flight list.");

        // get optId
        List<OptimizationEntry> optEList = ExcelInputReader.readOptimizationsFromFile(excelSource);
        
        // get initialFlightSequence
        String[] initialFlightSequence = new String[flights.size()];
        int i = 0;
        for (Flight f: flights) {
        	initialFlightSequence[i] = f.getFlightId();
        	i++;
        }
	
        // get flights (with weight map)
        Flight[] flightsArray = new Flight[flights.size()];
        int j = 0;
        for (Flight f: flights) {
        	flightsArray[j] = f;
        	j++;
        }
	
        // get slots
        OptimizationEntry opt = null;
        for (OptimizationEntry o: optEList) {
        	if (o.getOptId().equals(optId)) {
        		opt = o;
        	}
        }
        List<Slot> flightOrderPlaces = FlightGenerator.generateSequenceItems(opt);
        // convert Slot to SlotInfo
        SlotInfo[] slots = new SlotInfo[flightOrderPlaces.size()];
        int k = 0;
        for (Slot s: flightOrderPlaces) {
        	SlotInfo slot = new SlotInfo(s.getTime().atZone(ZoneId.of("Europe/Vienna")).toInstant());
        	slots[k] = slot;
        	k++;
        }
	
        // get optimizationFramework
        String framework = opt.getOptimizationFramework();
        if (framework.equals("null")) {
        	framework = null;
        }
	
        logger.debug("OptimizationSessionEntry: optId: " + optId + " | initialFlightSequence length: " + initialFlightSequence.length + 
        		" | flightsArray length: " + flightsArray.length + " | slots length: " + slots.length + " | optimizationFramework: " + framework);
        
        return new OptimizationSessionEntry(optId, initialFlightSequence, flightsArray, slots, framework);
            
	}
	
    /**
     * Read Margins and AssignedTime from given Excel input file and return them as a list of margins.
     *
     * @param fileName location and name of given input file
     * @return a list of margins
     */
    public static Map<MarginEntry,LocalDateTime> readMarginsAndResultFromFile(String fileName) {
        Map<MarginEntry,LocalDateTime> marginResultList = new LinkedHashMap<MarginEntry,LocalDateTime>();

        try (InputStream input = new FileInputStream(fileName)) {
            logger.info("Reading sheet 'Flights' from Excel file '" + fileName + "'.");
            logger.info("Reading margins from the file.");

            Workbook wb = WorkbookFactory.create(input);

            Sheet sheetFlights = wb.getSheet("Flights");

            int rowNum = 1; //information is in row 1 (second row) and below
            boolean eof = false;

            while (!eof) {
                Row row = sheetFlights.getRow(rowNum);

                if (row != null && row.getCell(0) != null){
                    Cell flightId = row.getCell(0);
                    Cell scheduledTime = row.getCell(1);
                    Cell timeNotBefore = row.getCell(2);
                    Cell timeWished = row.getCell(3);
                    Cell timeNotAfter = row.getCell(4);
                    Cell priority = row.getCell(5);

                    MarginEntry entry = new MarginEntry(
                        flightId.getStringCellValue(),
                        // set seconds and nanoseconds to zero (Excel sometimes adds small values to dates)
                        scheduledTime.getLocalDateTimeCellValue().withSecond(0).withNano(0),
                        timeNotBefore.getLocalDateTimeCellValue().withSecond(0).withNano(0),
                        timeWished.getLocalDateTimeCellValue().withSecond(0).withNano(0),
                        timeNotAfter.getLocalDateTimeCellValue().withSecond(0).withNano(0),
                        priority.getNumericCellValue()
                    );
                    
                    Cell assignedTime = row.getCell(5);
                    
                    marginResultList.put(entry, assignedTime.getLocalDateTimeCellValue().withSecond(0).withNano(0));

                    rowNum++;
                } else {
                    eof = true;
                }
            }
        } catch (IOException e) {
            logger.error("Could not read Excel file.", e);
        }

        return marginResultList;
    }

	/**
	 * Converts excel file (from fileName) which has to be in format as the files are, when weight maps
	 * have been written to them (as the output of writeWeightsToFile is), including margins (for generated JSON file).
	 * @param fileName Excel file name
	 * @param marginList list of margins
	 * @return OptimizationSessionEntry to be used for creating OptimizationDTO (SlotMachine-Optimizer)
	 */
	public static OptimizationSessionEntry convertToOptimizationSessionEntry(String fileName, List<MarginEntry> marginList) {
		OptimizationSessionEntry ose = convertToOptimizationSessionEntry(fileName);
		logger.info("Set Margin-List with given input.");
		List<MarginInfo> margins = new LinkedList<MarginInfo>();
		for (Flight f: ose.getFlights()) {
			MarginInfo currentMarginInfo = new MarginInfo();
			for (MarginEntry me: marginList) {
				if (f.getFlightId().equals(me.getFlightId())) {
					currentMarginInfo.setFlightId(f.getFlightId());
					currentMarginInfo.setScheduledTime(me.getScheduledTime().atZone(ZoneId.of("Europe/Vienna")).toInstant());
					currentMarginInfo.setTimeNotBefore(me.getTimeNotBefore().atZone(ZoneId.of("Europe/Vienna")).toInstant());
					currentMarginInfo.setTimeWished(me.getTimeWished().atZone(ZoneId.of("Europe/Vienna")).toInstant());
					currentMarginInfo.setTimeNotAfter(me.getTimeNotAfter().atZone(ZoneId.of("Europe/Vienna")).toInstant());
				}
			}
			if (currentMarginInfo.getTimeNotAfter() != null) {
				margins.add(currentMarginInfo);
			}
		}
		ose.setMargins(margins);
		return ose;
	}

	/**
	 * Converts excel file (from fileName) which has to be in format as the files are, when no weight maps
	 * have been written to them, including margins (for generated JSON file).
	 * @param excelSource excel source file
	 * @param generateFlights list of flights
	 * @param optId optimization id
	 * @param marginList list of margins
	 * @return OptimizationSessionEntry to be used for creating OptimizationDTO (SlotMachine-Optimizer)
	 */
	public static OptimizationSessionEntry convertToOptimizationSessionEntryNoWeightMap(String excelSource,
			List<Flight> generateFlights, String optId, List<MarginEntry> marginList) {
		OptimizationSessionEntry ose = convertToOptimizationSessionEntryNoWeightMap(excelSource, generateFlights, optId);
		logger.info("Set Margin-List with given input.");
		List<MarginInfo> margins = new LinkedList<MarginInfo>();
		for (Flight f: ose.getFlights()) {
			MarginInfo currentMarginInfo = new MarginInfo();
			for (MarginEntry me: marginList) {
				if (f.getFlightId().equals(me.getFlightId())) {
					currentMarginInfo.setFlightId(f.getFlightId());
					currentMarginInfo.setScheduledTime(me.getScheduledTime().atZone(ZoneId.of("Europe/Vienna")).toInstant());
					currentMarginInfo.setTimeNotBefore(me.getTimeNotBefore().atZone(ZoneId.of("Europe/Vienna")).toInstant());
					currentMarginInfo.setTimeWished(me.getTimeWished().atZone(ZoneId.of("Europe/Vienna")).toInstant());
					currentMarginInfo.setTimeNotAfter(me.getTimeNotAfter().atZone(ZoneId.of("Europe/Vienna")).toInstant());
				}
			}
			if (currentMarginInfo.getTimeNotAfter() != null) {
				margins.add(currentMarginInfo);
			}
		}
		ose.setMargins(margins);
		return ose;
	}
}
