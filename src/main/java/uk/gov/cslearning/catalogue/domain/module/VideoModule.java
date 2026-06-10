package uk.gov.cslearning.catalogue.domain.module;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;

@Setter
@Getter
@JsonTypeName("video")
public class VideoModule extends Module {

    private URL url;

    @JsonCreator
    public VideoModule(@JsonProperty("url") URL url) {
        this.type = "video";
        this.url = url;
    }

}
