package uk.gov.cslearning.catalogue.domain.module;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.net.URL;

@Setter
@Getter
@JsonTypeName("link")
public class LinkModule extends Module {

    @Field(type = FieldType.Text)
    private URL url;

    @JsonCreator
    public LinkModule(@JsonProperty("url") URL url) {
        this.type = "link";
        this.url = url;
    }

}
