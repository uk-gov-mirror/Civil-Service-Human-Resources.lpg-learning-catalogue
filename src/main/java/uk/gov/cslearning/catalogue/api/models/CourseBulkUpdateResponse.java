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
    private Collection<String> successfulIds;
    private Collection<String> failedIds;
}
