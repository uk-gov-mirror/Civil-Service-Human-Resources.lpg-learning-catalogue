package uk.gov.cslearning.catalogue.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkUpdateResponse<T> {
    private Collection<T> successfulIds;
    private Collection<T> failedIds;
}
