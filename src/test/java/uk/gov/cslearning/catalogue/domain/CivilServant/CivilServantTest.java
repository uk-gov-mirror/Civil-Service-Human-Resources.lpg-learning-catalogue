package uk.gov.cslearning.catalogue.domain.CivilServant;

import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.Assert.*;

@Transactional
public class CivilServantTest {

    public static final String ORGANISATIONAL_UNIT_CODE = "code";
    public static final long PROFESSION_ID = 1L;
    public static final String PROFESSION_NAME = "Profession Name";
    public static final String LEARNING_PROVIDER_UUID = "uuid";


    @Test
    public void shouldReturnEmptyOptionalIfCivilServantWithNoOrgProfLearningProv() {
        CivilServant civilServant = new CivilServant();

        assertFalse(civilServant.getOrganisationalUnitCode().isPresent());
        assertEquals(civilServant.getOrganisationalUnitCode(), Optional.empty());

        assertFalse(civilServant.getProfessionId().isPresent());
        assertEquals(civilServant.getProfessionId(), Optional.empty());

        assertFalse(civilServant.getProfessionName().isPresent());
        assertEquals(civilServant.getProfessionName(), Optional.empty());
    }

    @Test
    public void shouldReturnEmptyOptionalsIfCodeNotPresent() {
        CivilServant civilServant = new CivilServant();

        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        civilServant.setOrganisationalUnit(organisationalUnit);

        assertFalse(civilServant.getOrganisationalUnitCode().isPresent());
        assertEquals(civilServant.getOrganisationalUnitCode(), Optional.empty());
    }

    @Test
    public void shouldReturnOrgUnitCodeIfPresent() {
        CivilServant civilServant = new CivilServant();

        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        organisationalUnit.setCode(ORGANISATIONAL_UNIT_CODE);
        civilServant.setOrganisationalUnit(organisationalUnit);

        assertTrue(civilServant.getOrganisationalUnitCode().isPresent());
        assertEquals(civilServant.getOrganisationalUnitCode().get(), ORGANISATIONAL_UNIT_CODE);
    }

    @Test
    public void shouldReturnEmptyOptionalIfNoProfessionIdNamePresent() {
        CivilServant civilServant = new CivilServant();

        Profession profession = new Profession();
        civilServant.setProfession(profession);

        assertFalse(civilServant.getProfessionId().isPresent());
        assertEquals(civilServant.getProfessionId(), Optional.empty());

        assertFalse(civilServant.getProfessionName().isPresent());
        assertEquals(civilServant.getProfessionName(), Optional.empty());
    }


    @Test
    public void shouldReturnProfessionIfPresent() {
        CivilServant civilServant = new CivilServant();

        Profession profession = new Profession();
        profession.setId(PROFESSION_ID);
        profession.setName(PROFESSION_NAME);
        civilServant.setProfession(profession);

        assertTrue(civilServant.getProfessionId().isPresent());
        assertEquals(civilServant.getProfessionId().get(), Long.valueOf(1));

        assertTrue(civilServant.getProfessionName().isPresent());
        assertEquals(civilServant.getProfessionName().get(), PROFESSION_NAME);
    }

}
