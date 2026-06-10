package uk.gov.cslearning.catalogue.evaluators;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import uk.gov.cslearning.catalogue.domain.CivilServant.CivilServant;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.Roles;
import uk.gov.cslearning.catalogue.service.AuthoritiesService;
import uk.gov.cslearning.catalogue.service.CourseService;
import uk.gov.cslearning.catalogue.service.RegistryService;

import java.io.Serializable;
import java.util.function.Supplier;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomPermissionEvaluator.class);

    private RegistryService registryService;

    private CourseService courseService;

    private AuthoritiesService authoritiesService;

    public CustomPermissionEvaluator(RegistryService registryService, CourseService courseService, AuthoritiesService authoritiesService) {
        this.registryService = registryService;
        this.courseService = courseService;
        this.authoritiesService = authoritiesService;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if ((auth == null) || (targetDomainObject == null) || !(targetDomainObject instanceof String) || !(permission instanceof String) || !(targetDomainObject instanceof String)) {
            return false;
        }

        String id = (String) targetDomainObject;

        return hasScope(auth, id);
    }

    private boolean hasScope(Authentication auth, String id) {

        Course course = courseService.findById(id).orElseThrow((Supplier<IllegalStateException>) () -> {
            throw new IllegalStateException(
                    "Cannot get course");
        });

        if (course.getOwner() == null) {
            return false;
        }

        CivilServant civilServant = null;

        for (GrantedAuthority grantedAuth : auth.getAuthorities()) {
            LOGGER.info("User has authority: {}", grantedAuth.getAuthority());

            if (grantedAuth.getAuthority().equals(Roles.CSL_AUTHOR)) {
                return true;
            }
            if (grantedAuth.getAuthority().equals(Roles.LEARNING_MANAGER)) {
                return true;
            }

            if (grantedAuth.getAuthority().equals(Roles.ORGANISATION_AUTHOR)) {
                if (civilServant == null) civilServant = registryService.getCurrentCivilServant();
                if (authoritiesService.isOrganisationalUnitCodeEqual(civilServant, course.getOwner())) {
                    return true;
                }
            }
            if ((grantedAuth.getAuthority().equals(Roles.KPMG_SUPPLIER_AUTHOR)
                    || grantedAuth.getAuthority().equals(Roles.KORNFERRY_SUPPLIER_AUTHOR)
                    || grantedAuth.getAuthority().equals(Roles.KNOWLEDGEPOOL_SUPPLIER_AUTHOR)) && authoritiesService.isSupplierEqual(auth, course.getOwner())) {
                return true;
            }
        }
        return false;
    }
}
