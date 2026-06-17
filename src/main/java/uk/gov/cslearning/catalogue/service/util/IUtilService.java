package uk.gov.cslearning.catalogue.service.util;

import java.time.LocalDateTime;

public interface IUtilService {

    LocalDateTime getNowDateTime();

    String generateUrlSlugFromString(String name);
}
