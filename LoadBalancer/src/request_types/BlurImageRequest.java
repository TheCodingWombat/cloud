package request_types;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;

import utils.HttpRequestUtils;

public class BlurImageRequest extends AbstractRequestType {

	public static enum PictureFormat {
		BMP, JPEG, PNG, OTHER;

		public static PictureFormat ofString(String name) {
			switch (name) {
			case "jpeg":
				return JPEG;
			case "bmp":
				return BMP;
			case "png":
				return PNG;

			default:
				return OTHER;
			}
		}
	}

	private final PictureFormat pictureFormat;

	public BlurImageRequest(HttpExchange exchange) {
		this.pictureFormat = setPictureFormat(exchange);

	}

	private PictureFormat setPictureFormat(HttpExchange exchange) {
		String requestBody = HttpRequestUtils.getRequestBody(exchange);
		String dataTypeString = extractDataTypeString(requestBody);
		return PictureFormat.ofString(dataTypeString);
	}

	private String extractDataTypeString(String baseEncodedPicture) {
		int firstSlashIndex = baseEncodedPicture.indexOf('/');
		int firstSemicolonIndex = baseEncodedPicture.indexOf(';');

		if (firstSlashIndex != -1 && firstSemicolonIndex != -1) {
			String modifiedString = baseEncodedPicture.substring(firstSlashIndex + 1, firstSemicolonIndex);
			return modifiedString;
		} else {
			throw new IllegalArgumentException("Payload does not match expectation");
		}
	}
}