package uk.gov.cslearning.catalogue.api.v2.model;

import lombok.Data;
import uk.gov.cslearning.catalogue.domain.Status;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
public class CourseSearchParameters {
    String query = "";

    Collection<Status> status = Collections.singletonList(Status.PUBLISHED);
    String visibility = "PUBLIC";
    List<String> types = Collections.emptyList();
    String cost;

    List<String> courseIds = Collections.emptyList();
    List<String> departments = Collections.emptyList();
    List<String> areasOfWork = Collections.emptyList();
    List<String> interests = Collections.emptyList();

    String titleStartsWith;

    public boolean hasModuleTypes() {
        return !this.types.isEmpty();
    }

    public boolean hasAudienceFields() {
        return !departments.isEmpty() || !areasOfWork.isEmpty() || !interests.isEmpty();
    }

    public boolean costIsFree() {
        return this.cost != null && this.cost.equals("free");
    }
}
