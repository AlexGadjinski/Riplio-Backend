package app.common.storage;

import app.common.exception.InvalidFileException;
import app.config.properties.UploadProperties;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/**
 * Validates uploaded files by their actual content type and size.
 */
@Component
@RequiredArgsConstructor
public class FileValidator {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png");

    private static final String IMAGE_MIME_PREFIX = "image/";
    private static final String VIDEO_MIME_PREFIX = "video/";

    private final UploadProperties uploadProperties;
    private final Tika tika = new Tika();

    /**
     * Validates that the file is a supported image within the allowed size.
     */
    public void validateImage(MultipartFile file) {
        String detectedType = detectContentType(file);

        if (!ALLOWED_IMAGE_TYPES.contains(detectedType)) {
            throw new InvalidFileException("Only JPEG and PNG files are allowed.");
        }

        validateSize(file, uploadProperties.getMaxImageSize());
    }

    /**
     * Validates that the file is an image or video within the allowed size for its type.
     */
    public void validateMedia(MultipartFile file) {
        String detectedContentType = detectContentType(file);

        if (detectedContentType.startsWith(IMAGE_MIME_PREFIX)) {
            validateSize(file, uploadProperties.getMaxImageSize());
        } else if (detectedContentType.startsWith(VIDEO_MIME_PREFIX)) {
            validateSize(file, uploadProperties.getMaxVideoSize());
        } else {
            throw new InvalidFileException("Only image and video files are allowed.");
        }
    }

    /**
     * Detects the real MIME type from file content, ignoring the client-supplied Content-Type header.
     */
    private String detectContentType(MultipartFile file) {
        try {
            return tika.detect(file.getInputStream());
        } catch (IOException e) {
            throw new InvalidFileException("Failed to read file for validation.", e);
        }
    }

    /**
     * Throws if the file exceeds the given maximum size.
     */
    private void validateSize(MultipartFile file, DataSize maxSize) {
        if (file.getSize() > maxSize.toBytes()) {
            throw new InvalidFileException("File size exceeds the maximum allowed size of [%s].".formatted(maxSize));
        }
    }
}
