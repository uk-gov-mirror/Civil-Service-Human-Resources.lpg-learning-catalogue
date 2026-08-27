package uk.gov.cslearning.catalogue.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HyperlinkIdsDto {
    private List<Long> ids;
}
