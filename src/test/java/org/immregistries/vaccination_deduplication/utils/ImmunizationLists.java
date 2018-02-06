package org.immregistries.vaccination_deduplication.utils;

import org.immregistries.vaccination_deduplication.Immunization;
import org.immregistries.vaccination_deduplication.ImmunizationSource;
import org.immregistries.vaccination_deduplication.LinkedImmunization;
import org.immregistries.vaccination_deduplication.LinkedImmunizationType;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

public class ImmunizationLists {
    static ImmunizationLists instance;

    // setting them all to public to not have to create getters
    public LinkedImmunization patient1  =new LinkedImmunization();
    public LinkedImmunization patient2 = new LinkedImmunization();
    public LinkedImmunization patient3 = new LinkedImmunization();
    public Immunization immunization1 = new Immunization();
    public Immunization immunization2 = new Immunization();
    public Immunization immunization3 = new Immunization();
    public Immunization immunization4 = new Immunization();
    public Immunization immunization5 = new Immunization();
    public Immunization immunization6 = new Immunization();
    public Immunization immunization7 = new Immunization();
    public Immunization immunization8 = new Immunization();

    private ImmunizationLists() throws ParseException {
        patient1.setType(LinkedImmunizationType.INPUT);
        patient2.setType(LinkedImmunizationType.INPUT);
        patient3.setType(LinkedImmunizationType.INPUT);

        immunization1.setVaccineGroupList(new ArrayList<String>(Arrays.asList("MMR")));
        immunization1.setOrganisationID("Dr Murphey");
        immunization1.setDate("20161217");
        immunization1.setCVX("03");
        immunization1.setMVX("MSD");
        immunization1.setLotNumber("1");
        immunization1.setSource(ImmunizationSource.SOURCE);

        immunization2.setVaccineGroupList(new ArrayList<String>(Arrays.asList("MMR")));
        immunization2.setOrganisationID("Mercy Hospital");
        immunization2.setDate("20161217");
        immunization2.setCVX("03");
        immunization2.setMVX("MSD");
        immunization2.setLotNumber("1");
        immunization2.setSource(ImmunizationSource.HISTORICAL);

        immunization3.setVaccineGroupList(new ArrayList<String>(Arrays.asList("MMR")));
        immunization3.setOrganisationID("Medicare");
        immunization3.setDate("20161218");
        immunization3.setCVX("03");
        immunization3.setMVX("MSD");
        immunization3.setLotNumber("1");
        immunization3.setSource(ImmunizationSource.HISTORICAL);

        immunization4.setVaccineGroupList(new ArrayList<String>(Arrays.asList("DTaP", "IPV")));
        immunization4.setOrganisationID("Dr Murphey");
        immunization4.setDate("20161217");
        immunization4.setCVX("130");
        immunization4.setSource(ImmunizationSource.SOURCE);

        immunization5.setVaccineGroupList(new ArrayList<String>(Arrays.asList("Hep A")));
        immunization5.setOrganisationID("Dr Murphey");
        immunization5.setDate("20161217");
        immunization5.setCVX("83");
        immunization5.setSource(ImmunizationSource.SOURCE);

        immunization6.setVaccineGroupList(new ArrayList<String>(Arrays.asList("Hep B")));
        immunization6.setOrganisationID("Dr Murphey");
        immunization6.setDate("20160605");
        immunization6.setCVX("08");
        immunization6.setSource(ImmunizationSource.SOURCE);

        immunization7.setVaccineGroupList(new ArrayList<String>(Arrays.asList("Hep B")));
        immunization7.setOrganisationID("Dr Murphey");
        immunization7.setDate("20160401");
        immunization7.setCVX("08");
        immunization7.setSource(ImmunizationSource.SOURCE);


        immunization8.setVaccineGroupList(new ArrayList<String>(Arrays.asList("Hep B")));
        immunization8.setOrganisationID("Dr Murphey");
        immunization8.setDate("20151111");
        immunization8.setCVX("08");
        immunization8.setSource(ImmunizationSource.HISTORICAL);

        patient1.add(immunization1);
        patient1.add(immunization2);
        patient1.add(immunization3);
        patient1.add(immunization4);
        patient1.add(immunization5);
        patient1.add(immunization6);
        patient1.add(immunization7);
        patient1.add(immunization8);
    }

    public static ImmunizationLists getInstance() throws ParseException {
        if (ImmunizationLists.instance == null) {
            ImmunizationLists.instance = new ImmunizationLists();
        }
        return ImmunizationLists.instance;
    }
}
