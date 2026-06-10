package uk.gov.cslearning.catalogue.domain.module;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonTypeName("elearning")
public class ELearningModule extends Module {

    private String startPage;

    private String url;
    private String mediaId;

    @JsonCreator
    public ELearningModule(@JsonProperty("startPage") String startPage, @JsonProperty("url") String url) {
        this.type = "elearning";
        this.startPage = startPage;
        this.url = url;

    }

}

