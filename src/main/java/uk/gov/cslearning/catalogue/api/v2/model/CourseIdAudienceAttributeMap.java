package uk.gov.cslearning.catalogue.api.v2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseIdAudienceAttributeMap {

    private Map<String, Set<String>> areasOfWork;
    private Map<String, Set<String>> departments;
    private Map<String, Set<String>> interests;

}
