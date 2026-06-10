package uk.gov.cslearning.catalogue.domain.Owner;

import org.springframework.stereotype.Component;
import uk.gov.cslearning.catalogue.domain.CivilServant.CivilServant;

import java.util.Optional;

@Component
public class OwnerFactory {

    public Owner create(CivilServant civilServant) {
        Owner owner = new Owner();

        owner.setScope(civilServant.getScope().name());
        civilServant.getProfessionId().ifPresent(owner::setProfession);
        civilServant.getOrganisationalUnitCode().ifPresent(owner::setOrganisationalUnit);
        Optional.ofNullable(civilServant.getSupplier()).ifPresent(owner::setSupplier);

        return owner;
    }
}
