package at.jku.dke.slotmachine.data.excel;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.dke.slotmachine.data.excel.dto.TestDataConfigDTO;

public class ExcelIO {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        Options options = getCommandLineOptions();
        CommandLineParser parser = new DefaultParser();
        
        List<MarginEntry> margins = new LinkedList<MarginEntry>();
        List<OptimizationEntry> opts = new LinkedList<OptimizationEntry>();
        List<List<Flight>> flightLists = new LinkedList<List<Flight>>();
        List<Flight> flights = new LinkedList<Flight>();

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("h")) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.setWidth(120);
                helpFormatter.printHelp("Excel I/O", options);
                return;
            }
            
            // converts margins from Excel source files to JSON string
            if (line.hasOption("cm")) {
            	String fileSource = "";
            	String jsonTarget = "";
            	logger.info("convert margins from Excel source file to JSON string, uses only the first optimization entry");
            	
            	// look for given command line arguments of "cm"-option
            		// fileSource is required, jsonTarget is not required
            	if (line.getOptionValues("cm")[0].indexOf(".") != -1 && 
            			line.getOptionValues("cm")[0].substring(line.getOptionValues("cm")[0].lastIndexOf(".")).equals(".xlsx") &&
            			line.getOptionValues("cm").length == 1) {
            		// input file given, no output file
            		fileSource = line.getOptionValues("cm")[0];
            		logger.info("Source location for Excel file is: " + fileSource + ". Output file target is not given.");
            	} else if (line.getOptionValues("cm")[0].indexOf(".") != -1 && 
            			line.getOptionValues("cm")[0].substring(line.getOptionValues("cm")[0].lastIndexOf(".")).equals(".json") &&
            			line.getOptionValues("cm").length == 1) {
            		// input file given, no output file
            		jsonTarget = line.getOptionValues("cm")[0];
            		logger.info("Source location for Excel file is not given. Output file target is: " + jsonTarget + ".");
            	} else if (line.getOptionValues("cm")[0].indexOf(".") != -1 && line.getOptionValues("cm").length == 2 &&
            			line.getOptionValues("cm")[0].substring(line.getOptionValues("cm")[0].lastIndexOf(".")).equals(".xlsx") &&
            			line.getOptionValues("cm")[1].substring(line.getOptionValues("cm")[1].lastIndexOf(".")).equals(".json")) {
            		// input file given, output file given
            		fileSource = line.getOptionValues("cm")[0];
            		jsonTarget = line.getOptionValues("cm")[1];
            		logger.info("Source location for Excel file is: " + fileSource + ". Output file target is: " + jsonTarget + ".");            		
            	} else {
            		// no expected input
            		logger.info("No expected arguments found.");
            	}
            	if (fileSource.equals("")) {
            		// no input file
            		logger.info("No input given, therefore margins have not been converted.");
            	} else {
            		// input file given
                	margins = ExcelInputReader.readMarginsFromFile(fileSource);
                	opts = ExcelInputReader.readOptimizationsFromFile(fileSource);
                	for (OptimizationEntry o: opts) {
                		flightLists.add(FlightGenerator.generateFlights(margins, o));
                	}
                	//flights = FlightGenerator.generateFlights(margins, opts.get(0));
            	}
            	if (jsonTarget.equals("") && !(fileSource.equals(""))) {
            		// no output file given, print result to System.out
            		System.out.println(JsonOutputWriter.writeToString(flightLists));
            	} else if (!(fileSource.equals(""))){
            		// output file given, write result to file
                	JsonOutputWriter.writeToFile(jsonTarget, flightLists);
            	}
            }
            
            // converts margins from Excel source files to JSON string and writes weights to new Excel file (with generated name
            // according to the opt-id) -> this file has to be used for -wr (due to format)
            if (line.hasOption("cmw")) {
            	String fileSource = "";
            	String jsonTarget = "";
            	logger.info("convert margins from Excel source file to JSON string and writes weights to new Excel file");
            	
            	// look for given command line arguments of "cmw"-option
        			// fileSource is required, jsonTarget is not required
            	if (line.getOptionValues("cmw")[0].indexOf(".") != -1 && 
            			line.getOptionValues("cmw")[0].substring(line.getOptionValues("cmw")[0].lastIndexOf(".")).equals(".xlsx") &&
            			line.getOptionValues("cmw").length == 1) {
            		// input file given, no output file (json)
            		fileSource = line.getOptionValues("cmw")[0];
            		logger.info("Source location for Excel file is: " + fileSource + ". Output file target is not given.");
            	} else if (line.getOptionValues("cmw")[0].indexOf(".") != -1 && 
            			line.getOptionValues("cmw")[0].substring(line.getOptionValues("cmw")[0].lastIndexOf(".")).equals(".json") &&
            			line.getOptionValues("cmw").length == 1) {
            		// input file given, no output file (json)
            		jsonTarget = line.getOptionValues("cmw")[0];
            		logger.info("Source location for Excel file is not given. Output file target is: " + jsonTarget + ".");
            	} else if (line.getOptionValues("cmw")[0].indexOf(".") != -1 && line.getOptionValues("cmw").length == 2 &&
            			line.getOptionValues("cmw")[0].substring(line.getOptionValues("cmw")[0].lastIndexOf(".")).equals(".xlsx") &&
            			line.getOptionValues("cmw")[1].substring(line.getOptionValues("cmw")[1].lastIndexOf(".")).equals(".json")) {
            		// input file given, output file given (json)
            		fileSource = line.getOptionValues("cmw")[0];
            		jsonTarget = line.getOptionValues("cmw")[1];
            		logger.info("Source location for Excel file is: " + fileSource + ". Output file target is: " + jsonTarget + ".");            		
            	} else {
            		// no expected input
            		logger.info("No expected arguments found.");
            	}
            	if (fileSource.equals("")) {
            		// no input file
            		logger.info("No input given, therefore margins have not been converted.");
            	} else {
            		// input file given
                	margins = ExcelInputReader.readMarginsFromFile(fileSource);
                	opts = ExcelInputReader.readOptimizationsFromFile(fileSource);
                	for (OptimizationEntry o: opts) {
                		flightLists.add(FlightGenerator.generateFlights(margins, o));
                	}
            	}
            	if (jsonTarget.equals("") && !(fileSource.equals(""))) {
            		// no output file given, print result to System.out
            		System.out.println(JsonOutputWriter.writeToString(flightLists));
            		// generate new excel files with weights
            		for (OptimizationEntry o: opts) {
            			String fileName = "flights-" + o.getOptId() + ".xlsx";
            			ExcelOutputWriter.writeWeightsToFile(FlightGenerator.generateFlights(margins,o), o, fileName, fileSource);
            		}
            	} else if (!(fileSource.equals(""))){
            		// output file given, write result to file
                	JsonOutputWriter.writeToFile(jsonTarget, flightLists);
                	// generate new excel file with weights
            		for (OptimizationEntry o: opts) {
            			String fileName = "flights-" + o.getOptId() + ".xlsx";
            			ExcelOutputWriter.writeWeightsToFile(FlightGenerator.generateFlights(margins,o), o, fileName, fileSource);
            		}
            	}
            }
            
            // writes the results of optimization algorithms to an excel file (in the format of the result of -cmw)
            if(line.hasOption("wr")) {
            	String excelSource = "";
            	String jsonSource = "";
            	String excelTarget = "";
            	logger.info("write results of optimization algorithms to Excel file");
            	
            	// look for given command line arguments of "wr"-option
        			// excelSource is not required, jsonSource is required, excelTarget is required
            	if (line.getOptionValues("wr")[0].indexOf(".") != -1 &&
            			line.getOptionValues("wr")[0].substring(line.getOptionValues("wr")[0].lastIndexOf(".")).equals(".xlsx") &&
            			line.getOptionValues("wr").length == 1) {
            		// only one excel file given, could be either excelSource or excelTarget
            		boolean isTarget = ExcelInputReader.isTargetFile(line.getOptionValues("wr")[0]);
            		if (!isTarget) {
            			excelSource = line.getOptionValues("wr")[0];
                		logger.info("Source location for Excel file is: " + excelSource + ". " + 
                				"Source location for JSON file is not given. Output file target is not given."); 
            		} else {
            			excelTarget = line.getOptionValues("wr")[0];
                		logger.info("Source location for Excel file is not given. " + 
                				"Source location for JSON file is not given. Output file target is: " + excelTarget + "."); 
            		}
            	} else if (line.getOptionValues("wr")[0].indexOf(".") != -1 &&
            			line.getOptionValues("wr")[0].substring(line.getOptionValues("wr")[0].lastIndexOf(".")).equals(".json") &&
            			line.getOptionValues("wr").length == 1) {
            		jsonSource = line.getOptionValues("wr")[0];
            		logger.info("Source location for Excel file is not given. " + 
            				"Source location for JSON file is: " + jsonSource + ". Output file target is not given."); 
            	} else if (line.getOptionValues("wr")[0].indexOf(".") != -1 && line.getOptionValues("wr").length == 2 &&
            			line.getOptionValues("wr")[0].substring(line.getOptionValues("wr")[0].lastIndexOf(".")).equals(".xlsx") &&
            			line.getOptionValues("wr")[1].substring(line.getOptionValues("wr")[1].lastIndexOf(".")).equals(".json")) {
            		excelSource = line.getOptionValues("wr")[0];
            		jsonSource = line.getOptionValues("wr")[1];
            		logger.info("Source location for Excel file is: " + excelSource + ". " + 
            				"Source location for JSON file is: " + jsonSource + ". Output file target is not given."); 
            	} else if (line.getOptionValues("wr")[0].indexOf(".") != -1 && line.getOptionValues("wr").length == 2 &&
            			line.getOptionValues("wr")[0].substring(line.getOptionValues("wr")[0].lastIndexOf(".")).equals(".json") &&
            			line.getOptionValues("wr")[1].substring(line.getOptionValues("wr")[1].lastIndexOf(".")).equals(".xlsx")) {
            		jsonSource = line.getOptionValues("wr")[0];
            		excelTarget = line.getOptionValues("wr")[1];
            		logger.info("Source location for Excel file is not given. " + 
            				"Source location for JSON file is: " + jsonSource + ". Output file target is: " + excelTarget + "."); 
            	} else if (line.getOptionValues("wr")[0].indexOf(".") != -1 && line.getOptionValues("wr").length == 2 &&
            			line.getOptionValues("wr")[0].substring(line.getOptionValues("wr")[0].lastIndexOf(".")).equals(".xlsx") &&
            			line.getOptionValues("wr")[1].substring(line.getOptionValues("wr")[1].lastIndexOf(".")).equals(".xlsx")) {
            		excelSource = line.getOptionValues("wr")[0];
            		excelTarget = line.getOptionValues("wr")[1];
            		logger.info("Source location for Excel file is:" + excelSource + ". " + 
            				"Source location for JSON file is not given. Output file target is: " + excelTarget + "."); 
            	} else if (line.getOptionValues("wr")[0].indexOf(".") != -1 && line.getOptionValues("wr").length == 3 &&
            			line.getOptionValues("wr")[0].substring(line.getOptionValues("wr")[0].lastIndexOf(".")).equals(".xlsx") &&
            			line.getOptionValues("wr")[1].substring(line.getOptionValues("wr")[1].lastIndexOf(".")).equals(".json") &&
            			line.getOptionValues("wr")[2].substring(line.getOptionValues("wr")[2].lastIndexOf(".")).equals(".xlsx")) {
            		excelSource = line.getOptionValues("wr")[0];
            		jsonSource = line.getOptionValues("wr")[1];
            		excelTarget = line.getOptionValues("wr")[2];
            		logger.info("Source location for Excel file is: " + excelSource + ". " + 
            				"Source location for JSON file is: " + jsonSource + ". Output file target is: " + excelTarget + "."); 
            	} else {
            		logger.info("No expected arguments found.");
            	}
            	if (jsonSource.equals("")) {
            		// no JSON input
            		// necessary information from JSON source are the assigned slots (and the optId to create the 
            		// FlightSequenceEntry object)
            		logger.info("No input of JSON result file given, therefore no results have been written to target file.");
            	}
            	if (excelTarget.equals("")) {
            		// no excel target file
            		// nothing to return/update
            		if (!(jsonSource.equals(""))) {
            			logger.info("No target excel file was given, therefore no results have been written to target file.");
            		}
            	} else {
            		if (!(jsonSource.equals(""))) {
            			
            			OptimizationEntry optEntry = ExcelInputReader.readOptimizationFromTargetFile(excelTarget);
                    	FlightSequenceEntry sequence = JsonInputReader.readFlightSequenceFromFile(jsonSource);
                    	if (sequence.getOptId().equals(optEntry.getOptId())) {
                    		ExcelOutputWriter.writeFlightSequenceOrder(sequence, excelTarget, optEntry);
                    	} else {
                    		logger.info("Wrong file according to optimization session id! Therefore, no results have been written to target file.");
                    	}
            		}
            	}
            }
            
            // converts an excel file (in the format of -wr result (-cmw result should work as well)
            // to a JSON file, which can be used for SlotMachine-Optimizer (OptimizationDTO)
            if(line.hasOption("ctj")) {
            	String excelSource = "";
            	String jsonTarget = "";
            	OptimizationSessionEntry optSE = null;
            	logger.info("convert excel file with weight map to json");
            	
            	// look for given command line arguments of "ctj"-option
        			// excelSource is required, jsonTarget is not required
            	if (line.getOptionValues("ctj")[0].indexOf(".") != -1 && 
            			line.getOptionValues("ctj")[0].substring(line.getOptionValues("ctj")[0].lastIndexOf(".")).equals(".xlsx") &&
            			line.getOptionValues("ctj").length == 1) {
            		// input file given, no output file (json)
            		excelSource = line.getOptionValues("ctj")[0];
            		logger.info("Source location for Excel file is: " + excelSource + ". Output file target is not given.");
            	} else if (line.getOptionValues("ctj")[0].indexOf(".") != -1 && 
            			line.getOptionValues("ctj")[0].substring(line.getOptionValues("ctj")[0].lastIndexOf(".")).equals(".json") &&
            			line.getOptionValues("ctj").length == 1) {
            		// input file given, no output file (json)
            		jsonTarget = line.getOptionValues("ctj")[0];
            		logger.info("Source location for Excel file is not given. Output file target is: " + jsonTarget + ".");
            	} else if (line.getOptionValues("ctj")[0].indexOf(".") != -1 && line.getOptionValues("ctj").length == 2 &&
            			line.getOptionValues("ctj")[0].substring(line.getOptionValues("ctj")[0].lastIndexOf(".")).equals(".xlsx") &&
            			line.getOptionValues("ctj")[1].substring(line.getOptionValues("ctj")[1].lastIndexOf(".")).equals(".json")) {
            		// input file given, output file given (json)
            		excelSource = line.getOptionValues("ctj")[0];
            		jsonTarget = line.getOptionValues("ctj")[1];
            		logger.info("Source location for Excel file is: " + excelSource + ". Output file target is: " + jsonTarget + ".");            		
            	} else {
            		// no expected input
            		logger.info("No expected arguments found.");
            	}
            	
            	if (excelSource.equals("")) {
            		// no input file
            		logger.info("No input of excel source file given, therefore no conversion to JSON has been possible.");
            	} else {
            		// input file given
            		optSE = ExcelInputReader.convertToOptimizationSessionEntry(excelSource);
            	}
            	if (jsonTarget.equals("") && !(excelSource.equals(""))) {
            		// no output file given, print result to System.out
            		System.out.println(JsonOutputWriter.writeToString(optSE));
            	} else if (!(excelSource.equals(""))){
            		// output file given, write results to file
            		JsonOutputWriter.writeToFile(jsonTarget, optSE);
            	}
            }
            
            if (line.hasOption("cmtj")) {
            	logger.info("generate json file for optimizer with generated weight map from excel source file");
            	String excelSource = "";
            	String jsonTarget = "";
            	OptimizationSessionEntry optSE = null;
            	
            	// look for given command line arguments of "cmtj"-option
        			// excelSource is required, jsonTarget is not required
            	if (line.getOptionValues("cmtj")[0].indexOf(".") != -1 && 
            			line.getOptionValues("cmtj")[0].substring(line.getOptionValues("cmtj")[0].lastIndexOf(".")).equals(".xlsx") &&
            			line.getOptionValues("cmtj").length == 1) {
            		// input file given, no output file (json)
            		excelSource = line.getOptionValues("cmtj")[0];
            		logger.info("Source location for Excel file is: " + excelSource + ". Output file target is not given.");
            	} else if (line.getOptionValues("cmtj")[0].indexOf(".") != -1 && 
            			line.getOptionValues("cmtj")[0].substring(line.getOptionValues("cmtj")[0].lastIndexOf(".")).equals(".json") &&
            			line.getOptionValues("cmtj").length == 1) {
            		// input file given, no output file (json)
            		jsonTarget = line.getOptionValues("cmtj")[0];
            		logger.info("Source location for Excel file is not given. Output file target is: " + jsonTarget + ".");
            	} else if (line.getOptionValues("cmtj")[0].indexOf(".") != -1 && line.getOptionValues("cmtj").length == 2 &&
            			line.getOptionValues("cmtj")[0].substring(line.getOptionValues("cmtj")[0].lastIndexOf(".")).equals(".xlsx") &&
            			line.getOptionValues("cmtj")[1].substring(line.getOptionValues("cmtj")[1].lastIndexOf(".")).equals(".json")) {
            		// input file given, output file given (json)
            		excelSource = line.getOptionValues("cmtj")[0];
            		jsonTarget = line.getOptionValues("cmtj")[1];
            		logger.info("Source location for Excel file is: " + excelSource + ". Output file target is: " + jsonTarget + ".");            		
            	} else {
            		// no expected input
            		logger.info("No expected arguments found.");
            	}
            	
            	// write no excel file
            	if (!(excelSource.equals(""))) {
            		margins = ExcelInputReader.readMarginsFromFile(excelSource);
            		opts = ExcelInputReader.readOptimizationsFromFile(excelSource);
            		
            		// for each optimization entry generate a new file or console output
                	for (OptimizationEntry o: opts) {
                		flightLists.add(FlightGenerator.generateFlights(margins, o));
                		// use of other convert method, due to no existing weight map in excel file
                    	if (line.hasOption("margins")) {
                    		// if margins is active as an option, include margins in generated JSON file
                    		logger.info("Margins will be included in the generated JSON file.");
                    		optSE = ExcelInputReader.convertToOptimizationSessionEntryNoWeightMap(excelSource, 
                    				FlightGenerator.generateFlights(margins, o), o.getOptId(), margins);
                    	} else {
                    		optSE = ExcelInputReader.convertToOptimizationSessionEntryNoWeightMap(excelSource, 
                    				FlightGenerator.generateFlights(margins, o), o.getOptId());
                    	}
                		
                		if (!(jsonTarget.equals(""))) {
                    		// write to json file
                			String jsonFileName = jsonTarget.substring(0,jsonTarget.length()-5) + "-" + o.getOptId() + ".json";
                    		JsonOutputWriter.writeToFile(jsonFileName, optSE);
                    	} else {
                    		// write to console
                    		System.out.println(JsonOutputWriter.writeToString(optSE));          	
                    	}
                	}
            		

            	} else {
            		logger.info("No excel source file given, therefore it is not possible to generate the JSON file.");
            	}

            }
            
            if (line.hasOption("cmwetj")) {
            	logger.info("generate json file for optimizer with generated weight map from excel source file and generate new excel file");
            	String excelSource = "";
            	String jsonTarget = "";
            	OptimizationSessionEntry optSE = null;
            	
            	// look for given command line arguments of "cmwetj"-option
        			// excelSource is required, jsonTarget is not required
            	if (line.getOptionValues("cmwetj")[0].indexOf(".") != -1 && 
            			line.getOptionValues("cmwetj")[0].substring(line.getOptionValues("cmwetj")[0].lastIndexOf(".")).equals(".xlsx") &&
            			line.getOptionValues("cmwetj").length == 1) {
            		// input file given, no output file (json)
            		excelSource = line.getOptionValues("cmwetj")[0];
            		logger.info("Source location for Excel file is: " + excelSource + ". Output file target is not given.");
            	} else if (line.getOptionValues("cmwetj")[0].indexOf(".") != -1 && 
            			line.getOptionValues("cmwetj")[0].substring(line.getOptionValues("cmwetj")[0].lastIndexOf(".")).equals(".json") &&
            			line.getOptionValues("cmwetj").length == 1) {
            		// input file given, no output file (json)
            		jsonTarget = line.getOptionValues("cmwetj")[0];
            		logger.info("Source location for Excel file is not given. Output file target is: " + jsonTarget + ".");
            	} else if (line.getOptionValues("cmwetj")[0].indexOf(".") != -1 && line.getOptionValues("cmwetj").length == 2 &&
            			line.getOptionValues("cmwetj")[0].substring(line.getOptionValues("cmwetj")[0].lastIndexOf(".")).equals(".xlsx") &&
            			line.getOptionValues("cmwetj")[1].substring(line.getOptionValues("cmwetj")[1].lastIndexOf(".")).equals(".json")) {
            		// input file given, output file given (json)
            		excelSource = line.getOptionValues("cmwetj")[0];
            		jsonTarget = line.getOptionValues("cmwetj")[1];
            		logger.info("Source location for Excel file is: " + excelSource + ". Output file target is: " + jsonTarget + ".");            		
            	} else {
            		// no expected input
            		logger.info("No expected arguments found.");
            	}
            	
            	// write excel file
            	if (!(excelSource.equals(""))) {
            		margins = ExcelInputReader.readMarginsFromFile(excelSource);
            		opts = ExcelInputReader.readOptimizationsFromFile(excelSource);
                	for (OptimizationEntry o: opts) {
                		flights = FlightGenerator.generateFlights(margins, o);
                		flightLists.add(flights);
                		String fileName = "flights-" + o.getOptId() + ".xlsx";
                    	ExcelOutputWriter.writeWeightsToFile(flights, o, fileName, excelSource);
                    	if (line.hasOption("margins")) {
                    		// if margins is active as an option, include margins in generated JSON file
                    		logger.info("Margins will be included in the generated JSON file.");
                    		optSE = ExcelInputReader.convertToOptimizationSessionEntry(fileName, margins);
                    	} else {
                    		optSE = ExcelInputReader.convertToOptimizationSessionEntry(fileName);
                    	}
                    	if (!(jsonTarget.equals(""))) {
                    		// write to json file
                			String jsonFileName = jsonTarget.substring(0,jsonTarget.length()-5) + "-" + o.getOptId() + ".json";
                    		JsonOutputWriter.writeToFile(jsonFileName, optSE);
                    	} else {
                    		// write to console
                    		System.out.println(JsonOutputWriter.writeToString(optSE));          	
                    	}
                	}
            		
            	} else {
            		logger.info("No excel source file given, therefore it is not possible to generate the JSON file.");
            	}
            	
            }
            
            // generate test data
            if (line.hasOption("tdg")) {
            	logger.info("Generate test data for Optimizer.");
            	if (line.getOptionValue("tdg").substring(line.getOptionValue("tdg").lastIndexOf(".")).equals(".json")) {
            		TestDataConfigDTO testDataConfig = JsonInputReader.readTestDataConfigDTO(line.getOptionValue("tdg"));
            		logger.info("Successfully read test data generator configuration file.");
	            	String filesLocation = "";
	            	if (line.getOptionValue("tdg") != null) {
	            		filesLocation = line.getOptionValue("tdg");
	            	}
	            	TestDataGenerator.generateTestData(filesLocation, testDataConfig);
            	} else {
            		logger.info("No JSON file found. No test data generated.");
            	}
            }
            
            
        } catch (ParseException e) {
            logger.error("Could not parse command line options.", e);
        }
    }

    private static Options getCommandLineOptions() {
        Options options = new Options();

        Option help = new Option("h", "help", false, "print this message");

        options.addOption(help);
        // (arguments are optional, at least one argument is required to get some results)
        
        // -cm
        //		with two correct arguments -cm functions correct
        //		with the excel source location missing, it does not generate json target file
        // 		with the json target file name missing, it does print json content instead of creating a file
        Option convertMargins = new Option("cm", "convertMargins", true, "convert margins from Excel source file to JSON target file, uses only the first optimization entry \n" + 
        																 "- first argument is Excel source location (required for functionality) " + 
        																 "\n - second argument is JSON target file name (if not given the result is printed to the console) \n");   
        convertMargins.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(convertMargins);
        
        // -cmw
        //		with two correct arguments -cmw functions correct
        //		with the excel source location missing, it does not generate json target file and excel target file
        // 		with the json target file name missing, it does print json content instead of creating a file and generate excel target file
        Option convertMarginsWrite = new Option("cmw", "convertMarginsWriteWeight", true, "convert margins from Excel source file to JSON string and writes weights to new Excel file, uses only the first optimization entry \n" + 
				 "- first argument is Excel source location (required for functionality) " + 
				 "\n - second argument is JSON target file name (if not given the result is printed to the console) \n");   
        convertMarginsWrite.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(convertMarginsWrite);
        
        // -cmtj
        //		with two correct arguments -cmtj functions correct
        //		with the excel source location missing, it does not generate json target file
        // 		with the json target file name missing, it does print json content instead of creating a file
        Option convertMarginsToJson = new Option("cmtj", "convertMarginsToJson", true, "generate JSON file for optimizer with generated weight map from excel source file \n" +
				 "- first argument is Excel source location (required for functionality) " + 
				 "\n - second argument is JSON target file name (if not given the result is printed to the console) \n");   
        convertMarginsToJson.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(convertMarginsToJson);
        
        // -cmwetj
        //		with two correct arguments -cmwetj functions correct
        //		with the excel source location missing, it does not generate json target file and excel target file
        // 		with the json target file name missing, it does print json content instead of creating a file and generate excel target file
        Option convertMarginsWriteExcelToJson = new Option("cmwetj", "convertMarginsWriteExcelToJson", true, "generate JSON file for optimizer with generated weight map from excel source file and generate new excel file \n" +
				 "- first argument is Excel source location (required for functionality) " + 
				 "\n - second argument is JSON target file name (if not given the result is printed to the console) \n");   
        convertMarginsWriteExcelToJson.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(convertMarginsWriteExcelToJson);
        
        // -ctj
        //		with two correct arguments -ctj functions correct
        //		with the excel source location missing, it does not generate json target file
        // 		with the json target file name missing, it does print json content instead of creating a file
        Option convertToJson = new Option("ctj", "convertToJson", true, "convert excel file with weight map to json \n" +
				 "- first argument is Excel source location (required for functionality) " + 
				 "\n - second argument is JSON target file name (if not given the result is printed to the console) \n");  
        convertToJson.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(convertToJson);
        
        // -wr
        //		with two correct arguments -wr functions correct
        //		with the excel source location missing, it uses data from excel target 
        // 		with the json source location missing, it does not write results to file
        //		with the excel target location missing, it has to file to write results to
        Option writeResults = new Option("wr", "writeResults", true, "write results of optimization algorithms to Excel file \n" +
        		"- first argument is Excel source location (if not given, information from Excel target location is used)\n " + 
        		"- second argument is JSON source location (required for functionality)\n " + 
        		"- third argument is Excel target location (with weight maps) (required for functionality)");
        writeResults.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(writeResults);
        
        // -margins
        //		without arguments
        //		works with -cmtj/-cmwetj (e.g. -cmtj <args> -margins)
        //		changes the JSON file generation to include margins per flight
        Option includeMargins = new Option("margins", "includeMargins", false, "include margins (for -cmtj, -cmwetj) for the generated JSON file \n" +
        		"(does not need arguments)");
        //includeMargins.setArgs(0);
        includeMargins.setOptionalArg(true);
        options.addOption(includeMargins);
        
        // -tdg
        //		needs one argument:
        //		filesName/Location: the JSON file name with the test data generator configuration and at the same folder level the 
        //							generated files will be stored
        Option testDataGenerator = new Option("tdg", "testDataGenerator", true, "generate test data for Optimizer and write generated files to location \n" +
        		"- first argument is the JSON file name (with location) of the configuration for the test data generator \n" +
        		"  and the location for the generated files (required)");
        includeMargins.setArgs(1);
        options.addOption(testDataGenerator);
        
        // -costf
        //		needs one argument:
        //		costfunction: 'linearSteps' - linear cost function with steps; TODO add other cost function
//        Option costfunction = new Option("costf", "costfunction", true, "set cost function to generate weight maps \n" +
//        		"- first argument is the type of cost function (required)");
        // TODO at FlightGenerator.generateWeightMap-"places" - changes would be required to allow different cost functions
        
        return options;
    }
}
