package uk.gov.cslearning.catalogue.domain.Owner;

import org.junit.Test;
import uk.gov.cslearning.catalogue.domain.CivilServant.CivilServant;
import uk.gov.cslearning.catalogue.domain.CivilServant.OrganisationalUnit;
import uk.gov.cslearning.catalogue.domain.CivilServant.Profession;
import uk.gov.cslearning.catalogue.domain.Scope;

import static org.junit.Assert.assertEquals;

public class OwnerFactoryTest {

    public static final Scope GLOBAL_SCOPE = Scope.GLOBAL;
    public static final Long PROFESSION_ID = 1L;
    public static final String ORGANISATIONAL_UNIT_CODE = "code";

    @Test
    public void shouldCreateOwnerFromCivilServantAndCourse() {
        CivilServant civilServant = new CivilServant();
        civilServant.setScope(GLOBAL_SCOPE);

        Profession profession = new Profession();
        profession.setId(PROFESSION_ID);
        civilServant.setProfession(profession);

        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        organisationalUnit.setCode(ORGANISATIONAL_UNIT_CODE);
        civilServant.setOrganisationalUnit(organisationalUnit);

        OwnerFactory ownerFactory = new OwnerFactory();

        Owner owner = ownerFactory.create(civilServant);

        assertEquals(owner.getScope(), GLOBAL_SCOPE.name());
        assertEquals(owner.getOrganisationalUnit(), ORGANISATIONAL_UNIT_CODE);
        assertEquals(owner.getProfession(), PROFESSION_ID);
    }
}
