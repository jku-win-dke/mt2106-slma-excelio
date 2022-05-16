package at.jku.dke.slotmachine.data.excel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class JsonOutputWriter {
    private static final Logger logger = LogManager.getLogger();

    /**
     * Writes content of object to file
     * @param fileName name of file, which is created/updated
     * @param object object, which is used as the content for the file
     */
    public static void writeToFile(String fileName, Object object) {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();

        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        try {
            File file = new File(fileName);
            writer.writeValue(file, object);
            logger.info("Writing information to file '" + fileName + "'.");
        } catch (IOException e) {
            logger.error("Cannot write JSON to file '" + fileName + "'.", e);
        }
    }
    
    /**
     * Stores the content to a String object
     * @param object object, which contains the content
     */
    public static String writeToString(Object object) {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();

        try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
		} catch (JsonProcessingException e) {
			logger.error("Cannot process object.");
			e.printStackTrace();
			return "{}";
		}
    }
}
