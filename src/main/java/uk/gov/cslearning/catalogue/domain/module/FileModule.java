package uk.gov.cslearning.catalogue.domain.module;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonTypeName("file")
public class FileModule extends Module {

    private String url;
    private Long fileSize;
    private String mediaId;

    @JsonCreator
    public FileModule(@JsonProperty("url") String url, @JsonProperty("fileSize") Long fileSize) {
        this.type = "file";
        this.url = url;
        this.fileSize = fileSize;
    }

}
