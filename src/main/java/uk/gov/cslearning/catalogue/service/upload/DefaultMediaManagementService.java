package uk.gov.cslearning.catalogue.service.upload;

import org.springframework.stereotype.Service;
import uk.gov.cslearning.catalogue.domain.Media;
import uk.gov.cslearning.catalogue.domain.MediaFactory;
import uk.gov.cslearning.catalogue.dto.upload.FileUpload;
import uk.gov.cslearning.catalogue.dto.upload.Upload;
import uk.gov.cslearning.catalogue.repository.elastic.MediaRepository;

import java.util.Locale;
import java.util.Optional;

@Service
public class DefaultMediaManagementService implements MediaManagementService {
    private final MediaFactory mediaFactory;
    private final MediaRepository mediaRepository;
    private final FileUploadServiceFactory fileUploadServiceFactory;

    public DefaultMediaManagementService(MediaFactory mediaFactory, MediaRepository mediaRepository,
                                         FileUploadServiceFactory fileUploadServiceFactory) {
        this.mediaFactory = mediaFactory;
        this.mediaRepository = mediaRepository;
        this.fileUploadServiceFactory = fileUploadServiceFactory;
    }

    @Override
    public Media create(FileUpload fileUpload) {
        String fileExt = fileUpload.getExtension().toLowerCase(Locale.ROOT);
        FileUploadService fileUploadService = fileUploadServiceFactory.getFileUploadServiceWithExt(fileExt);
        Upload upload = fileUploadService.upload(fileUpload);
        Media media = mediaFactory.create(upload);

        return mediaRepository.save(media);
    }

    @Override
    public Optional<Media> findById(String mediaId) {
        return mediaRepository.findById(mediaId);
    }
}
