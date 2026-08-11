package uk.gov.cslearning.catalogue.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BulkUpdateDto {

    Collection<Long> successfulUpdates;
    Collection<Long> failedUpdates;

}
