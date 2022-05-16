package at.jku.dke.slotmachine.data.excel;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import at.jku.dke.slotmachine.data.excel.dto.TestDataConfigDTO;

public class JsonInputReader {
    private static final Logger logger = LogManager.getLogger();

    /**
     * Used to read flight sequences from json files
     * @param fileName json file name/location
     * @return returns a FlightSequenceEntry-Object
     */
    public static FlightSequenceEntry readFlightSequenceFromFile(String fileName) {
    	logger.info("Reading JSON file from '" + fileName + "'.");
    	ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
    	try {
			FlightSequenceEntry flightSequenceEntry = mapper.readValue(new File(fileName), FlightSequenceEntry.class);
			logger.info("FlightSequenceEntry sucessfully created from JSON file.");
			return flightSequenceEntry;
		} catch (JsonParseException e) {
			e.printStackTrace();
			return null;
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    }

	public static TestDataConfigDTO readTestDataConfigDTO(String fileName) {
    	logger.info("Reading JSON file from '" + fileName + "'.");
    	ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
    	try {
    		TestDataConfigDTO testDataConfigDTO = mapper.readValue(new File(fileName), TestDataConfigDTO.class);
			logger.info("TestDataConfigDTO sucessfully created from JSON file.");
			return testDataConfigDTO;
		} catch (JsonParseException e) {
			e.printStackTrace();
			return null;
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
