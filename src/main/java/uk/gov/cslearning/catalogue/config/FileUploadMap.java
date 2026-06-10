package uk.gov.cslearning.catalogue.config;

import org.springframework.stereotype.Component;
import uk.gov.cslearning.catalogue.service.upload.UploadServiceType;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FileUploadMap {

    List<FileMapping> fileMappings = Arrays.asList(
            new FileMapping("doc", UploadServiceType.FILE, "file"),
            new FileMapping("docx", UploadServiceType.FILE, "file"),
            new FileMapping("pdf", UploadServiceType.FILE, "file"),
            new FileMapping("ppsm", UploadServiceType.FILE, "file"),
            new FileMapping("ppt", UploadServiceType.FILE, "file"),
            new FileMapping("pptx", UploadServiceType.FILE, "file"),
            new FileMapping("xls", UploadServiceType.FILE, "file"),
            new FileMapping("xlsx", UploadServiceType.FILE, "file"),
            new FileMapping("zip", UploadServiceType.SCORM, "elearning"),
            new FileMapping("mp4", UploadServiceType.MP4, "video"),
            new FileMapping("jpg", UploadServiceType.IMAGE, ""),
            new FileMapping("jpeg", UploadServiceType.IMAGE, ""),
            new FileMapping("png", UploadServiceType.IMAGE, ""),
            new FileMapping("svg", UploadServiceType.IMAGE, "")
    );

    Map<String, UploadServiceType> extToUploadServiceMap;
    Map<String, UploadServiceType> moduleToUploadServiceMap;

    @PostConstruct
    void init() {
        extToUploadServiceMap = new HashMap<>();
        moduleToUploadServiceMap = new HashMap<>();
        this.fileMappings.forEach(fm -> {
            extToUploadServiceMap.put(fm.getExt(), fm.getServiceType());
            moduleToUploadServiceMap.put(fm.getModuleType(), fm.getServiceType());
        });
    }

    public List<String> getValidFileExts() {
        return this.fileMappings.stream().map(FileMapping::getExt).collect(Collectors.toList());
    }

    public UploadServiceType getServiceTypeFromExt(String ext) {
        return this.extToUploadServiceMap.get(ext);
    }

}
