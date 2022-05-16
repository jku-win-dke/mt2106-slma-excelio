package at.jku.dke.slotmachine.data.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelOutputWriter {
	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * write weights to fileName, for each optimization
	 * @param flights list of flights with weight map
	 * @param opt OptimizationEntry, to get Optimization-Id and times of the flightorder-places
	 * @param fileName filename of the file, which should be created
	 * @param sourceName filename of the file, which is the 'origin'
	 */
	public static void writeWeightsToFile(List<Flight> flights, OptimizationEntry opt, String fileName, String sourceName) {
        try (InputStream input = new FileInputStream(sourceName)) {
            logger.info("Reading sheet 'Flights' from Excel file '" + sourceName + "'.");

            Workbook wbSource = WorkbookFactory.create(input);
            
			XSSFWorkbook wb = new XSSFWorkbook();
			logger.info("Create new Excel Workbook.");
			// create bold style
			Font fontBold = wb.createFont();
			fontBold.setBold(true);
			CellStyle boldStyle = wb.createCellStyle();
			boldStyle.setFont(fontBold);
			boldStyle.setBorderBottom(BorderStyle.MEDIUM);
			
			// create date style
			CellStyle dateStyle = wb.createCellStyle();
			dateStyle.setDataFormat(
					wb.getCreationHelper()
					.createDataFormat()
					.getFormat("dd.mm.yyyy hh:mm")
					);
			
			// create date style right alignment
			CellStyle dateStyleLeft = wb.createCellStyle();
			dateStyleLeft.setDataFormat(
					wb.getCreationHelper()
					.createDataFormat()
					.getFormat("dd.mm.yyyy hh:mm")
					);
			dateStyleLeft.setAlignment(HorizontalAlignment.LEFT);
			
			// create right alignment style
			CellStyle leftAlignment = wb.createCellStyle();
			leftAlignment.setAlignment(HorizontalAlignment.LEFT);
			
			Sheet sheetFlightNew = wb.createSheet("Flights");
			// set widths
			sheetFlightNew.setColumnWidth(1, 4000);
			sheetFlightNew.setColumnWidth(2, 4000);
			sheetFlightNew.setColumnWidth(3, 4000);
			sheetFlightNew.setColumnWidth(4, 4000);
			sheetFlightNew.setColumnWidth(5, 2000);
			sheetFlightNew.setColumnWidth(6, 4000);
			sheetFlightNew.setColumnWidth(7, 6500);
			sheetFlightNew.setColumnWidth(8, 10000);
			
			// create first sheet for flights
			logger.info("Create title sheet.");
			Row row = sheetFlightNew.createRow(0);
			Cell titleCell = row.createCell(0);
			titleCell.setCellValue("FlightId");
			titleCell.setCellStyle(boldStyle);
			titleCell = row.createCell(1);
			titleCell.setCellValue("ScheduledTime");
			titleCell.setCellStyle(boldStyle);
			titleCell = row.createCell(2);
			titleCell.setCellValue("TimeNotBefore");
			titleCell.setCellStyle(boldStyle);
			titleCell = row.createCell(3);
			titleCell.setCellValue("TimeWished");
			titleCell.setCellStyle(boldStyle);
			titleCell = row.createCell(4);
			titleCell.setCellValue("TimeNotAfter");
			titleCell.setCellStyle(boldStyle);
			titleCell = row.createCell(5);
			titleCell.setCellValue("priority");
			titleCell.setCellStyle(boldStyle);
			titleCell = row.createCell(6);
			titleCell.setCellValue("AssignedSlot");
			titleCell.setCellStyle(boldStyle);
			titleCell = row.createCell(7);
			titleCell.setCellValue("Optimization-ID:");
			titleCell.setCellStyle(boldStyle);
			titleCell = row.createCell(8);
			titleCell.setCellValue(opt.getOptId());
			Cell subtitleCell = sheetFlightNew.createRow(1).createCell(7);
			subtitleCell.setCellValue("Optimization-Framework:");
			subtitleCell = sheetFlightNew.getRow(1).createCell(8);
			if (!(opt.getOptimizationFramework().equals("null")) || (opt.getOptimizationFramework() != null)) {
				subtitleCell.setCellValue(opt.getOptimizationFramework());
			} else {
				subtitleCell.setCellValue("");
			}
			subtitleCell.setCellStyle(leftAlignment);
			subtitleCell = sheetFlightNew.createRow(2).createCell(7);
			subtitleCell.setCellValue("Optimization-StartTime:");
			subtitleCell = sheetFlightNew.getRow(2).createCell(8);
			subtitleCell.setCellValue(opt.getStartTime().withNano(0).withSecond(0));
			subtitleCell.setCellStyle(dateStyleLeft);
			subtitleCell = sheetFlightNew.createRow(3).createCell(7);
			subtitleCell.setCellValue("Optimization-EndTime:");
			subtitleCell = sheetFlightNew.getRow(3).createCell(8);
			subtitleCell.setCellValue(opt.getEndTime());
			subtitleCell.setCellStyle(dateStyleLeft);
			subtitleCell = sheetFlightNew.createRow(4).createCell(7);
			subtitleCell.setCellValue("Optimization-Interval [sec]:");
			subtitleCell = sheetFlightNew.getRow(4).createCell(8);
			subtitleCell.setCellValue(opt.getInterval());
			subtitleCell.setCellStyle(leftAlignment);
			subtitleCell = sheetFlightNew.createRow(5).createCell(7);
			subtitleCell.setCellValue("Optimization-minValue:");
			subtitleCell = sheetFlightNew.getRow(5).createCell(8);
			subtitleCell.setCellValue(opt.getMinValue());
			subtitleCell.setCellStyle(leftAlignment);
			subtitleCell = sheetFlightNew.createRow(6).createCell(7);
			subtitleCell.setCellValue("Optimization-maxValue:");
			subtitleCell = sheetFlightNew.getRow(6).createCell(8);
			subtitleCell.setCellValue(opt.getMaxValue());
			subtitleCell.setCellStyle(leftAlignment);
			subtitleCell = sheetFlightNew.createRow(7).createCell(7);
			subtitleCell.setCellValue("Optimization-dropValue:");
			subtitleCell = sheetFlightNew.getRow(7).createCell(8);
			subtitleCell.setCellValue(opt.getDropValue());
			subtitleCell.setCellStyle(leftAlignment);
			
			
			
			// create Slots for flight order time
			List<Slot> flightOrderPlaces = FlightGenerator.generateSequenceItems(opt);
			
	
			
			// write weights to separate sheets
			logger.info("Creating seperate sheets for each flight with assigned weight map.");
			for (Flight f: flights) {
				// write title row
				Sheet sheetWeight = wb.createSheet(f.getFlightId());
				Row row0 = sheetWeight.createRow(0);
				Cell cell0_0 = row0.createCell(0);
				cell0_0.setCellValue("Slot");
				cell0_0.setCellStyle(boldStyle);
				Cell cell0_1 = row0.createCell(1);
				cell0_1.setCellValue("Weight");
				cell0_1.setCellStyle(boldStyle);
				sheetWeight.setColumnWidth(0, 4000);
				
				// write current flight to title sheet and do not overwrite existing rows
				if (sheetFlightNew.getRow(flights.indexOf(f)+1) == null) {
					sheetFlightNew
						.createRow(flights.indexOf(f)+1)
						.createCell(0)
						.setCellValue(f.getFlightId());
				} else {
					sheetFlightNew
					.getRow(flights.indexOf(f)+1)
					.createCell(0)
					.setCellValue(f.getFlightId());
				}
				// 'copy' ScheduledTime, TimeNotBefore, TimeWished, TimeNotAfter
					// ScheduledTime
				Row currentRowFlight = sheetFlightNew.getRow(flights.indexOf(f)+1);
				Cell currentCellFlight = currentRowFlight.createCell(1);
				currentCellFlight.setCellValue(wbSource.getSheet("Flights").getRow(flights.indexOf(f)+1).getCell(1).getLocalDateTimeCellValue().withNano(0).withSecond(0));
				currentCellFlight.setCellStyle(dateStyle);
					// TimeNotBefore
				currentCellFlight = currentRowFlight.createCell(2);
				currentCellFlight.setCellValue(wbSource.getSheet("Flights").getRow(flights.indexOf(f)+1).getCell(2).getLocalDateTimeCellValue().withNano(0).withSecond(0));
				currentCellFlight.setCellStyle(dateStyle);
					// TimeWished
				currentCellFlight = currentRowFlight.createCell(3);
				currentCellFlight.setCellValue(wbSource.getSheet("Flights").getRow(flights.indexOf(f)+1).getCell(3).getLocalDateTimeCellValue().withNano(0).withSecond(0));
				currentCellFlight.setCellStyle(dateStyle);
					// TimeNotAfter
				currentCellFlight = currentRowFlight.createCell(4);
				currentCellFlight.setCellValue(wbSource.getSheet("Flights").getRow(flights.indexOf(f)+1).getCell(4).getLocalDateTimeCellValue().withNano(0).withSecond(0));
				currentCellFlight.setCellStyle(dateStyle);
					// priority
				currentCellFlight = currentRowFlight.createCell(5);
				currentCellFlight.setCellValue(wbSource.getSheet("Flights").getRow(flights.indexOf(f)+1).getCell(5).getNumericCellValue());
				currentCellFlight.setCellStyle(leftAlignment);
				
				// write weight to current flight
				int currentRowPos = 1;
				for (Slot s: flightOrderPlaces) {
					Row currentRow = sheetWeight.createRow(currentRowPos);
					Cell currentCell = currentRow.createCell(0);
					currentCell.setCellValue(s.getTime().withNano(0).withSecond(0));
					currentCell.setCellStyle(dateStyle);
					currentCell = currentRow.createCell(1);
					currentCell.setCellValue(f.getWeightMap()[currentRowPos-1]);
					currentRowPos++;
				}					
			}
			
			// write information to file
			try (OutputStream fileOut = new FileOutputStream(fileName)) {		
			    wb.write(fileOut);
		        wb.close();
		        logger.info("Finished creating the Excel file " + fileName + ".");
			} catch (IOException e) {
	            logger.error("Could not read Excel file.", e);
	        }
        } catch (IOException e){
        	e.printStackTrace();
        }

	}
	
	/**
	 * writes flight sequence into excel target file (expects sheet with margins included)
	 * @param sequence flight sequence with positions and optimization id
	 * @param excelFile file, which should be edited
	 */
	public static void writeFlightSequenceOrder(FlightSequenceEntry sequence, String excelFile, OptimizationEntry opt) {
		if (opt == null || sequence == null || excelFile == null) {
			logger.error("Input parameters have been wrong. No FlightSequence or no excel file given or no existing optimization entry does belong to flight sequence.");
			logger.error("Given optimization id was: " + sequence.getOptId() + ".");
			return;
		}
        try (InputStream input = new FileInputStream(excelFile)) {
            logger.info("Reading from Excel file '" + excelFile + "'.");

            Workbook wb = WorkbookFactory.create(input);

    		// create bold style
    		Font fontBold = wb.createFont();
    		fontBold.setBold(true);
    		CellStyle boldStyle = wb.createCellStyle();
    		boldStyle.setFont(fontBold);
    		boldStyle.setBorderBottom(BorderStyle.MEDIUM);
    		
    		// create date style
    		CellStyle dateStyle = wb.createCellStyle();
    		dateStyle.setDataFormat(
    				wb.getCreationHelper()
    				.createDataFormat()
    				.getFormat("dd.mm.yyyy hh:mm")
    				);
            
            Sheet sheetFlights = wb.getSheet("Flights");
            sheetFlights.setColumnWidth(6, 4000);
            
			Row row0 = sheetFlights.getRow(0);
			Cell cell0_6 = row0.createCell(6);
			cell0_6.setCellValue("AssignedSlot");
			cell0_6.setCellStyle(boldStyle);
			
			logger.info("Style and settings set for file.");
			
			boolean eof = false;
			List<String> flightIdOrder = new LinkedList<String>();
			int pos = 0;
			do {
				pos++;
				if(sheetFlights.getRow(pos) == null) {
					eof = true;
				} else {
					Row currentRow = sheetFlights.getRow(pos);
	
					if(currentRow.getCell(0) == null || currentRow.getCell(0).getStringCellValue().equals("")) {
						eof = true;
					} else {
						String currentCell = currentRow.getCell(0).getStringCellValue();
						flightIdOrder.add(currentCell);
					}
				}
			} while (!eof);
			
			List<Slot> flightOrderPlaces = FlightGenerator.generateSequenceItems(opt);
			
			for(int i = 0; i < sequence.getOptimizedFlightSequence().length; i++) {
				// get position of current flight (in sequence) (-> j)
				int j = -1;
				int k = 0;
				for (String s: flightIdOrder) {
					if (s.equals(sequence.getOptimizedFlightSequence()[i])){
						j = k+1;
					}
					k++;
				}
				// only continue if found
				if (j != -1) {
					Row currentRow = sheetFlights.getRow(j);
					Cell currentCell = currentRow.createCell(6);
					currentCell.setCellValue(flightOrderPlaces.get(i).getTime().withNano(0).withSecond(0));
					currentCell.setCellStyle(dateStyle);
				}
			}
			

			
			// write information to file
			try (OutputStream fileOut = new FileOutputStream(excelFile)) {		
			    wb.write(fileOut);
		        wb.close();
		        logger.info("Finished creating/updating the Excel file '" + excelFile + "'.");
			} catch (IOException e) {
	            logger.error("Could not read Excel file.", e);
	        }
			
			/*if(logger.isInfoEnabled() || logger.isDebugEnabled()) {
				// additional information to print to console
				Map<MarginEntry,LocalDateTime> marginResults = ExcelInputReader.readMarginsAndResultFromFile(excelFile);
				int beforeScheduled = 0;
				int beforeNotBefore = 0;
				int afterNotAfter = 0;
				int closeToWished = 0; // closer than 5 minutes
				 for (Map.Entry<MarginEntry,LocalDateTime> entry: marginResults.entrySet()) {
					 if (entry.getKey().getScheduledTime().isAfter(entry.getValue())) {
						 beforeScheduled++;
					 }
					 if (entry.getKey().getTimeNotBefore().isAfter(entry.getValue())) {
						 beforeNotBefore++;
					 }
					 if (entry.getKey().getTimeNotAfter().isBefore(entry.getValue())) {
						 afterNotAfter++;
					 }
					 if (Duration.between(entry.getKey().getTimeWished(), entry.getValue()).toMinutes() < 5 &&
							 Duration.between(entry.getKey().getTimeWished(), entry.getValue()).toMinutes() > -5) {
						 closeToWished++;
					 }
				 }
				 logger.info("Analysis of assigned times related to given margins:");
				 logger.info("AssignedSlot before ScheduledTime: " + (double) beforeScheduled/marginResults.size()*100 + "% (" + beforeScheduled + " flights)");
				 logger.info("AssignedSlot before TimeNotBefore: " + (double) beforeNotBefore/marginResults.size()*100 + "% (" + beforeNotBefore + " flights)");
				 logger.info("AssignedSlot after TimeNotAfter: " + (double) afterNotAfter/marginResults.size()*100 + "% (" + afterNotAfter + " flights)");
				 logger.info("AssignedSlot closer than 5 minutes to TimeWished: " + (double) closeToWished/marginResults.size()*100 + "% (" + closeToWished + " flights)");
			}*/
        } catch (IOException e) {
            logger.error("Could not read Excel file.", e);
        }
	}
}
