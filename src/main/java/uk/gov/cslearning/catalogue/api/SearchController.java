package uk.gov.cslearning.catalogue.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cslearning.catalogue.Utils;
import uk.gov.cslearning.catalogue.api.v2.model.CourseSearchParameters;
import uk.gov.cslearning.catalogue.domain.CivilServant.CivilServant;
import uk.gov.cslearning.catalogue.repository.CourseRepository;
import uk.gov.cslearning.catalogue.service.AuthoritiesService;
import uk.gov.cslearning.catalogue.service.RegistryService;

@RestController
@RequestMapping("/search")
public class SearchController {

    private CourseRepository courseRepository;

    private RegistryService registryService;

    private AuthoritiesService authoritiesService;

    @Autowired
    public SearchController(CourseRepository courseRepository, RegistryService registryService, AuthoritiesService authoritiesService) {
        this.courseRepository = courseRepository;
        this.registryService = registryService;
        this.authoritiesService = authoritiesService;
    }

    @GetMapping
    public ResponseEntity<SearchResults> search(CourseSearchParameters parameters, PageParameters pageParameters) {
        SearchResults results = courseRepository.search(pageParameters.getPageRequest(), parameters);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/management/courses")
    public ResponseEntity<SearchResults> searchForOrganisation(
            Authentication authentication, CourseSearchParameters parameters, PageParameters pageParameters) {
        OwnerParameters ownerParameters = new OwnerParameters();
        Pageable pageable = pageParameters.getPageRequest();
        if (Utils.hasRoles(new String[]{"CSL_AUTHOR", "LEARNING_MANAGER"})) {
            return ResponseEntity.ok(courseRepository.search(pageable, parameters));
        } else {
            if (Utils.hasRole("ORGANISATION_AUTHOR")) {
                CivilServant civilServant = registryService.getCurrentCivilServant();
                civilServant.getOrganisationalUnitCode().ifPresent(ownerParameters::setOrganisationalUnitCode);
                return ResponseEntity.ok(courseRepository.search(pageable, parameters, ownerParameters));
            } else if (Utils.hasRoles(new String[]{"KPMG_SUPPLIER_AUTHOR"})) {
                ownerParameters.setSupplier(authoritiesService.getSupplier(authentication));
                return ResponseEntity.ok(courseRepository.search(pageable, parameters, ownerParameters));
            }
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
