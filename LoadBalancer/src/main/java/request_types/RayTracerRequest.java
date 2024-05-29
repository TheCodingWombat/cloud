package request_types;

import com.sun.net.httpserver.HttpExchange;

import utils.QueryStringParser;

public class RayTracerRequest extends AbstractRequestType {

	private final int scols;
	private final int srows;
	private final int wcols;
	private final int wrows;
	private final int coff;
	private final int roff;
	private final boolean aa;

	public RayTracerRequest(HttpExchange exchange, String requestBody) {
		QueryStringParser parser = new QueryStringParser(exchange.getRequestURI().getQuery());
		scols = parser.getParameterValueOfInteger("scols");
		srows = parser.getParameterValueOfInteger("srows");
		wcols = parser.getParameterValueOfInteger("wcols");
		wrows = parser.getParameterValueOfInteger("wrows");
		coff = parser.getParameterValueOfInteger("coff");
		roff = parser.getParameterValueOfInteger("roff");
		aa = parser.getParameterValueOfBoolean("aa");
		System.out.println(toString());
	}

	@Override
	public String toString() {
		return "RayTracerRequest [scols=" + scols + ", srows=" + srows + ", wcols=" + wcols + ", wrows=" + wrows
				+ ", coff=" + coff + ", roff=" + roff + ", aa=" + aa + "]";
	}
	@Override
	public String toJson(){
		return "{\n" +
				"  \"scols\": " + scols + ",\n" +
				"  \"srows\": " + srows + ",\n" +
				"  \"wcols\": " + wcols + ",\n" +
				"  \"wrows\": " + wrows + ",\n" +
				"  \"coff\": " + coff + ",\n" +
				"  \"roff\": " + roff + ",\n" +
				"  \"aa\": " + aa + "\n" +
				"}";
	}

	@Override
	public double[] toXArray() {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public String serializeCsv() {
		return scols + ";" + srows + ";" + wcols + ";" + wrows + ";" + coff + ";" + roff + ";" + aa + ";";
	}
}