package metric_storage_system;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import request_types.AbstractRequestType;
import utils.CSVSerializable;

public class CsvExporter {

	static String folderPath = "metricsOutput";
	static String currentTimeStamp = getCurrentTimeStamp();

	public static void mapToCsv(Map<AbstractRequestType, RequestMetrics> map) {

		try (FileWriter writer = new FileWriter(folderPath + "//" + currentTimeStamp + ".csv")) {
			for (Entry<AbstractRequestType, RequestMetrics> entry : map.entrySet()) {
				CSVSerializable key = entry.getKey();
				CSVSerializable value = entry.getValue();

				writer.append(key.serializeCsv());
				writer.append(value.serializeCsv());
				writer.append(System.lineSeparator());
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not write to file");
		}
	}

	private static String getCurrentTimeStamp() {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
		return now.format(formatter);
	}
}