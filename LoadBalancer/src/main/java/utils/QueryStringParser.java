package utils;

import java.util.HashMap;
import java.util.Map;

/**
 * This class parses the query string of a webrequest
 */
public class QueryStringParser {

	private final String queryString;

	public QueryStringParser(String queryString) {
		super();
		this.queryString = queryString;
	}
	
	public int getParameterValueOfInteger(String queryString) {
		return Integer.valueOf(getParameterValue(queryString));
	}
	
	public boolean getParameterValueOfBoolean(String queryString) {
		return Boolean.valueOf(getParameterValue(queryString));
	}

	private String getParameterValue(String parameterName) {
		String[] params = queryString.split("&");
		Map<String, String> parameters = new HashMap<>();

		for (String param : params) {
			String[] keyValue = param.split("=");
			if (keyValue.length >= 2) {
				parameters.put(keyValue[0], keyValue[1]);
			}
		}

		if (parameters.containsKey(parameterName)) {
			return parameters.get(parameterName);
		}
		throw new RuntimeException("Parameter " + parameterName + " could not be found in the query!");
	}
}