package uk.gov.cslearning.catalogue.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.data.elasticsearch.annotations.FieldType.Date;

@Setter
@Getter
@Document(indexName = "media")
public class Media {
    @Id
    private String id;

    @NotNull
    private String name;

    @NotNull
    private String container;

    @NotNull
    @Field(type = Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateAdded;

    @NotNull
    private String path;

    private Map<String, String> metadata = new HashMap<>();

    private long fileSizeKB;
    private String extension;

    @JsonProperty
    public String formatFileSize() {
        long sizeBytes = fileSizeKB * 1024;
        if (sizeBytes <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(sizeBytes) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(sizeBytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @JsonIgnore
    public String getMetadataWithCustomKey(CustomMediaMetadata key) {
        return this.metadata.get(key.getMetadataKey());
    }
}
