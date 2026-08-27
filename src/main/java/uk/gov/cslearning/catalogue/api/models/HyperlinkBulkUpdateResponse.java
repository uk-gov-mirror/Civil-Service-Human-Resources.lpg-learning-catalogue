package uk.gov.cslearning.catalogue.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HyperlinkBulkUpdateResponse {
    private Collection<Long> successfulIds;
    private Collection<Long> failedIds;
}
