package uk.gov.cslearning.catalogue.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import uk.gov.cslearning.catalogue.domain.CivilServant.CivilServant;
import uk.gov.cslearning.catalogue.domain.CivilServant.OrganisationalUnit;
import uk.gov.cslearning.catalogue.domain.CivilServant.Profession;
import uk.gov.cslearning.catalogue.domain.Owner.Owner;
import uk.gov.cslearning.catalogue.domain.Roles;
import uk.gov.cslearning.catalogue.domain.Scope;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AuthoritiesServiceTest {

    private static final String ORGANISATIONAL_UNIT_CODE = "code";
    private static final Long PROFESSION_ID = 1L;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthoritiesService authoritiesService;

    @Before
    public void setupMock() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnGlobalIfCSLAuthor() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(Roles.CSL_AUTHOR));

        Mockito.doReturn(authorities).when(authentication).getAuthorities();

        assertEquals(authoritiesService.getScope(authentication), Scope.GLOBAL);
        assertNotEquals(authoritiesService.getScope(authentication), Scope.LOCAL);
    }

    @Test
    public void shouldReturnLocalIfNotCSLAuthor() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(Roles.SUPPLIER_AUTHOR));

        Mockito.doReturn(authorities).when(authentication).getAuthorities();

        assertEquals(authoritiesService.getScope(authentication), Scope.LOCAL);
        assertNotEquals(authoritiesService.getScope(authentication), Scope.GLOBAL);
    }

    @Test
    public void shouldReturnTrueIfLearningManager() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(Roles.LEARNING_MANAGER));

        Mockito.doReturn(authorities).when(authentication).getAuthorities();

        assertTrue(authoritiesService.isLearningManager(authentication));
        assertFalse(authoritiesService.isCslAuthor(authentication));
        assertFalse(authoritiesService.isOrgAuthor(authentication));
        assertFalse(authoritiesService.isProfessionAuthor(authentication));
    }

    @Test
    public void shouldReturnTrueIfCslAuthor() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(Roles.CSL_AUTHOR));

        Mockito.doReturn(authorities).when(authentication).getAuthorities();

        assertTrue(authoritiesService.isCslAuthor(authentication));
        assertFalse(authoritiesService.isLearningManager(authentication));
        assertFalse(authoritiesService.isOrgAuthor(authentication));
        assertFalse(authoritiesService.isProfessionAuthor(authentication));
    }

    @Test
    public void shouldReturnTrueIfOrgAuthor() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(Roles.ORGANISATION_AUTHOR));

        Mockito.doReturn(authorities).when(authentication).getAuthorities();

        assertTrue(authoritiesService.isOrgAuthor(authentication));
        assertFalse(authoritiesService.isCslAuthor(authentication));
        assertFalse(authoritiesService.isLearningManager(authentication));
        assertFalse(authoritiesService.isProfessionAuthor(authentication));
    }

    @Test
    public void shouldReturnTrueIfProfessionAuthor() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(Roles.PROFESSION_AUTHOR));

        Mockito.doReturn(authorities).when(authentication).getAuthorities();

        assertTrue(authoritiesService.isProfessionAuthor(authentication));
        assertFalse(authoritiesService.isOrgAuthor(authentication));
        assertFalse(authoritiesService.isCslAuthor(authentication));
        assertFalse(authoritiesService.isLearningManager(authentication));
    }

    @Test
    public void shouldReturnTrueIfOrganisationCodeEqual() {
        CivilServant civilServant = new CivilServant();

        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        organisationalUnit.setCode(ORGANISATIONAL_UNIT_CODE);
        civilServant.setOrganisationalUnit(organisationalUnit);

        Owner owner = new Owner();
        owner.setOrganisationalUnit(ORGANISATIONAL_UNIT_CODE);

        assertTrue(authoritiesService.isOrganisationalUnitCodeEqual(civilServant, owner));
    }

    @Test
    public void shouldReturnFalseIfOrganisationCodeNotEqual() {
        CivilServant civilServant = new CivilServant();

        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        organisationalUnit.setCode(ORGANISATIONAL_UNIT_CODE);
        civilServant.setOrganisationalUnit(organisationalUnit);

        Owner owner = new Owner();
        owner.setOrganisationalUnit("nocode");

        assertFalse(authoritiesService.isOrganisationalUnitCodeEqual(civilServant, owner));
    }

    @Test
    public void shouldReturnFalseIfCivilServantHasNoOrganisationCode() {
        CivilServant civilServant = new CivilServant();

        Owner owner = new Owner();
        owner.setOrganisationalUnit(ORGANISATIONAL_UNIT_CODE);

        assertFalse(authoritiesService.isOrganisationalUnitCodeEqual(civilServant, owner));
    }

    @Test
    public void shouldReturnFalseIfOwnerHasNoOrganisationCode() {
        CivilServant civilServant = new CivilServant();

        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        organisationalUnit.setCode(ORGANISATIONAL_UNIT_CODE);
        civilServant.setOrganisationalUnit(organisationalUnit);

        Owner owner = new Owner();

        assertFalse(authoritiesService.isOrganisationalUnitCodeEqual(civilServant, owner));
    }


    @Test
    public void shouldReturnTrueIfProfessionIdEqual() {
        CivilServant civilServant = new CivilServant();

        Profession profession = new Profession();
        profession.setId(PROFESSION_ID);
        civilServant.setProfession(profession);

        Owner owner = new Owner();
        owner.setProfession(PROFESSION_ID);

        assertTrue(authoritiesService.isProfessionIdEqual(civilServant, owner));
    }

    @Test
    public void shouldReturnFalseIfProfessionIdNotEqual() {
        CivilServant civilServant = new CivilServant();

        Profession profession = new Profession();
        profession.setId(PROFESSION_ID);
        civilServant.setProfession(profession);

        Owner owner = new Owner();
        owner.setProfession(2L);

        assertFalse(authoritiesService.isProfessionIdEqual(civilServant, owner));
    }

    @Test
    public void shouldReturnFalseIfCivilServantHasNoProfession() {
        CivilServant civilServant = new CivilServant();

        Owner owner = new Owner();
        owner.setProfession(PROFESSION_ID);

        assertFalse(authoritiesService.isProfessionIdEqual(civilServant, owner));
    }

    @Test
    public void shouldReturnFalseIfOwnerHasNoProfession() {
        CivilServant civilServant = new CivilServant();

        Profession profession = new Profession();
        profession.setId(PROFESSION_ID);
        civilServant.setProfession(profession);

        Owner owner = new Owner();

        assertFalse(authoritiesService.isProfessionIdEqual(civilServant, owner));
    }
}
