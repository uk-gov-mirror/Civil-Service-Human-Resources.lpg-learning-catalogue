package uk.gov.cslearning.catalogue.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseBulkUpdateResponse {

    @JsonProperty("successful_ids")
    private Collection<String> successfulIds;

    @JsonProperty("failed_ids")
    private Collection<String> failedIds;
}
