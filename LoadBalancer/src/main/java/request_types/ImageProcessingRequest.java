package request_types;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import com.sun.net.httpserver.HttpExchange;

import utils.PictureFormat;

public class ImageProcessingRequest extends AbstractRequestType {

	public final PictureFormat pictureFormat;
	private final int width;
	private final int height;
	private final int pixelCount;
	private final long totalSizeInBytes;
	
	public ImageProcessingRequest(HttpExchange exchange, String requestBody) {
		this.pictureFormat = setPictureFormat(requestBody);
		BufferedImage image = base64ToBufferedImage(requestBody);
		width = image.getWidth();
		height = image.getHeight();
		pixelCount = image.getWidth() * image.getHeight(); 
		totalSizeInBytes = calculateTotalSizeInBytes(image);
	}

	//there are doubts if this is right
	private long calculateTotalSizeInBytes(BufferedImage image) {
		ColorModel colorModel = image.getColorModel();
		int bytesPerPixel = colorModel.getPixelSize();
		return (long) pixelCount * bytesPerPixel;
	}

	private PictureFormat setPictureFormat(String requestBody) {
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

	private BufferedImage base64ToBufferedImage(String base64DecodedPicture) {
		String cleanBase64String = convertToCleanString(base64DecodedPicture);
		return base64ToImage(cleanBase64String);
	}

	private String convertToCleanString(String base64DecodedPicture) {
		int firstSemicolonIndex = base64DecodedPicture.indexOf(',');
		return base64DecodedPicture.substring(firstSemicolonIndex + 1);
	}

	private BufferedImage base64ToImage(String base64DecodedPicture) {
		byte[] imageBytes = Base64.getDecoder().decode(base64DecodedPicture);
		BufferedImage image;
		try {
			image = ImageIO.read(new ByteArrayInputStream(imageBytes));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (image == null) {
			throw new RuntimeException("Picture could not be loaded");
		}
		return image;
	}
	@Override
	public String toJson() {
		return "{\n" +
				"  \"pictureFormat\": \"" + pictureFormat + "\",\n" +
				"  \"width\": " + width + ",\n" +
				"  \"height\": " + height + ",\n" +
				"  \"pixelCount\": " + pixelCount + ",\n" +
				"  \"totalSizeInBytes\": " + totalSizeInBytes + "\n" +
				"}";
	}

	// To array of features used in model
	@Override
	public double[] toXArray() {
		return new double[] {pixelCount};
	}
	
	@Override
	public String serializeCsv() {
		return pictureFormat + ";" + width + ";" + height + ";" + pixelCount + ";" + totalSizeInBytes + ";";
	}
}