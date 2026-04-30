package uk.gov.cslearning.catalogue.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.stereotype.Service;
import uk.gov.cslearning.catalogue.domain.CivilServant.CivilServant;
import uk.gov.cslearning.catalogue.domain.CivilServant.OrganisationalUnit;
import uk.gov.cslearning.catalogue.exception.IllegalTypeException;
import uk.gov.cslearning.catalogue.service.record.RequestEntityFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class RegistryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryService.class);

    private OAuth2RestOperations restOperations;

    private URI getCurrentUrl;
    private String getAllCodes;

    private String getOrganisationalUnit;

    private final RequestEntityFactory requestEntityFactory;

    private final ParameterizedTypeReferenceFactory parameterizedTypeReferenceFactory;

    public RegistryService(OAuth2RestOperations restOperations,
                           RequestEntityFactory requestEntityFactory,
                           @Value("${registry.getCurrentUrl}") URI getCurrentUrl,
                           @Value("${registry.getOrganisationalUnit}") String getOrganisationalUnit,
                           @Value("${registry.getAllCodes}") String getAllCodes,
                           ParameterizedTypeReferenceFactory parameterizedTypeReferenceFactory) {
        this.restOperations = restOperations;
        this.requestEntityFactory = requestEntityFactory;
        this.getCurrentUrl = getCurrentUrl;
        this.getOrganisationalUnit = getOrganisationalUnit;
        this.parameterizedTypeReferenceFactory = parameterizedTypeReferenceFactory;
        this.getAllCodes = getAllCodes;
    }

    @PreAuthorize("isAuthenticated()")
    public CivilServant getCurrentCivilServant() {
        LOGGER.debug("Getting profile details for authenticated user");
        RequestEntity requestEntity = requestEntityFactory.createGetRequest(getCurrentUrl);

        ResponseEntity<CivilServant> response = restOperations.exchange(requestEntity, CivilServant.class);

        return response.getBody();
    }

    public List<OrganisationalUnit> getOrganisationalUnit(String department) {
        LOGGER.debug("Getting profile details for authenticated user");
        String activationUrl = String.format(getOrganisationalUnit, department);

        RequestEntity requestEntity = requestEntityFactory.createGetRequest(activationUrl);

        ResponseEntity<List<OrganisationalUnit>> response = null;
        try {
            response = restOperations.exchange(requestEntity, parameterizedTypeReferenceFactory.createListReference(OrganisationalUnit.class));
        } catch (IllegalTypeException e) {
            e.printStackTrace();
        }
        if (response.getBody() != null) {
            return response.getBody();
        }
        return new ArrayList<>();
    }

}
