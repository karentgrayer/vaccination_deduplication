package org.immregistries.vaccination_deduplication;
import junit.framework.TestCase;

import org.immregistries.vaccination_deduplication.computation_classes.Deterministic;
import org.immregistries.vaccination_deduplication.computation_classes.Hybrid;
import org.immregistries.vaccination_deduplication.computation_classes.Weighted;

public class HybridTest extends TestCase{
    public void testScoreDifferent() throws Exception {
        Immunization immunization1 = new Immunization();
        Immunization immunization2 = new Immunization();

 
        immunization1.setDate("20171225");
        immunization1.setLotNumber("test lot number 1");
        immunization1.setCVX("VCX 1");
        immunization1.setMVX("MVX 1");
        immunization1.setProductCode("pc 2");
        immunization1.setSource(ImmunizationSource.SOURCE);
        immunization1.setOrganisationID("20171225");

        immunization2.setDate("20101102");
        immunization2.setLotNumber("test lot number 2");
        immunization2.setCVX("VCX 2");
        immunization2.setMVX("MVX 2");
        immunization2.setProductCode("pc 2");
        immunization2.setSource(ImmunizationSource.ALTERNATE);
        immunization2.setOrganisationID("20171225");

        Weighted weighted = new Weighted(new PropertyLoader().getParameters());
        Deterministic deterministic = new Deterministic();

        ComparisonResult scoreWeighted = weighted.compare(immunization1, immunization2);
        ComparisonResult scoreDeterministic = deterministic.compare(immunization1, immunization2);

        Hybrid hybrid = new Hybrid(new PropertyLoader().getParameters());
       	assertEquals(ComparisonResult.DIFFERENT,hybrid.compare(scoreWeighted,scoreDeterministic));
    }
    }

