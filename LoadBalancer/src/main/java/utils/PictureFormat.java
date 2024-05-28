package utils;

public enum PictureFormat {
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