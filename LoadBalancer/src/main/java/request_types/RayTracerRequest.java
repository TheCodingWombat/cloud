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
	private final boolean multi;

	public RayTracerRequest(HttpExchange exchange, String requestBody) {
		QueryStringParser parser = new QueryStringParser(exchange.getRequestURI().getQuery());
		scols = parser.getParameterValueOfInteger("scols");
		srows = parser.getParameterValueOfInteger("srows");
		wcols = parser.getParameterValueOfInteger("wcols");
		wrows = parser.getParameterValueOfInteger("wrows");
		coff = parser.getParameterValueOfInteger("coff");
		roff = parser.getParameterValueOfInteger("roff");
		aa = parser.getParameterValueOfBoolean("aa");
		multi = parser.getParameterValueOfBoolean("multi");
		System.out.println(toString());
	}

	@Override
	public String toString() {
		return "RayTracerRequest [scols=" + scols + ", srows=" + srows + ", wcols=" + wcols + ", wrows=" + wrows
				+ ", coff=" + coff + ", roff=" + roff + ", aa=" + aa + ", multi=" + multi + "]";
	}

	@Override
	public double[] toXArray() {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}