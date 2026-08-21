package app.post.model;

public enum PostMediaType {
    IMAGE, GIF, VIDEO;

    private static final String IMAGE_MIME_PREFIX = "image/";
    private static final String VIDEO_MIME_PREFIX = "video/";
    private static final String GIF_MIME_TYPE = "image/gif";

    public static PostMediaType fromContentType(String contentType) {

        if (contentType.startsWith(VIDEO_MIME_PREFIX)) {
            return VIDEO;
        } else if (GIF_MIME_TYPE.equals(contentType)) {
            return GIF;
        } else if (contentType.startsWith(IMAGE_MIME_PREFIX)) {
            return IMAGE;
        }

        throw new IllegalArgumentException("Unsupported media content type: [%s].".formatted(contentType));
    }
}
