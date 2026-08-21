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
 * Validates uploaded files based on their actual content type and size.
 * <p>
 * The content type is detected from the file content rather than relying on the
 * client-provided {@code Content-Type} header.
 * </p>
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
     * Validates that the file is a supported image and does not exceed the maximum configured
     * image size.
     */
    public String validateImage(MultipartFile file) {
        String detectedContentType = detectContentType(file);

        if (!ALLOWED_IMAGE_TYPES.contains(detectedContentType)) {
            throw new InvalidFileException("Only JPEG and PNG files are allowed.");
        }

        validateSize(file, uploadProperties.getMaxImageSize());
        return detectedContentType;
    }

    /**
     * Validates that the file is an image or video and does not exceed the corresponding
     * configured size limit.
     */
    public String validateMedia(MultipartFile file) {
        String detectedContentType = detectContentType(file);

        if (detectedContentType.startsWith(IMAGE_MIME_PREFIX)) {
            validateSize(file, uploadProperties.getMaxImageSize());
        } else if (detectedContentType.startsWith(VIDEO_MIME_PREFIX)) {
            validateSize(file, uploadProperties.getMaxVideoSize());
        } else {
            throw new InvalidFileException("Only image and video files are allowed.");
        }

        return detectedContentType;
    }

    /**
     * Detects the actual MIME type from the file content.
     */
    private String detectContentType(MultipartFile file) {
        try {
            return tika.detect(file.getInputStream());
        } catch (IOException e) {
            throw new InvalidFileException("Failed to read file for validation.", e);
        }
    }

    /**
     * Validates that the file does not exceed the specified maximum size.
     */
    private void validateSize(MultipartFile file, DataSize maxSize) {
        if (file.getSize() > maxSize.toBytes()) {
            throw new InvalidFileException("File size exceeds the maximum allowed size of [%s].".formatted(maxSize));
        }
    }
}
