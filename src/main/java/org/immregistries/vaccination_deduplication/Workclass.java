package org.immregistries.vaccination_deduplication;

import org.immregistries.vaccination_deduplication.computation_classes.*;
import org.immregistries.vaccination_deduplication.utils.ImmunizationNormalisation;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * 
 * Launch the deduplication process according to the evaluation method chosen
 * which can be the weighted approach, the deterministic approach or a combination of both
 *
 */
// TODO change name
public class Workclass {
    //ImmunizationNormalisation immunizationNormalisation;

    public Workclass(String codebaseFilePath) throws FileNotFoundException {
        //this.immunizationNormalisation = ImmunizationNormalisation.getInstance();
        //this.immunizationNormalisation.initialize(codebaseFilePath);
    }

    public Workclass() {
        //this.immunizationNormalisation = ImmunizationNormalisation.getInstance();
        //this.immunizationNormalisation.initialize();
    }

    public void refreshCodebase(String codebaseFilePath) throws FileNotFoundException {
        //this.immunizationNormalisation.refreshCodebase(codebaseFilePath);
    }


    public boolean lineHas(ArrayList<Result> line, Result result) {
        for (Result r : line)
            if (r.equals(result))
                return true;

        return false;
    }

    public ArrayList<LinkedImmunization> postprocessing(LinkedImmunization toEvaluate, ArrayList<ArrayList<Result>> results) {
        HashMap<Integer, LinkedImmunization> groups = new HashMap<Integer, LinkedImmunization>();
        ArrayList<LinkedImmunization> unsures = new ArrayList<LinkedImmunization>();
        ArrayList<LinkedImmunization> differents = new ArrayList<LinkedImmunization>();

        for (int i = 0; i < results.size(); i++) {
            boolean lineHasEqual = false;
            LinkedImmunization unsure = new LinkedImmunization();
            unsure.setType(LinkedImmunization.TYPE.UNSURE);
            unsure.add(toEvaluate.get(i));

            LinkedImmunization different = new LinkedImmunization();
            different.add(toEvaluate.get(i));
            different.setType(LinkedImmunization.TYPE.DIFFERENT);

            for (int j = 0; j < results.size(); j++) {
                if (results.get(i).get(j).equals(Result.EQUAL)) {
                    lineHasEqual = true;
                    if(groups.containsKey(i) && groups.containsKey(j)) {

                    } else if(groups.containsKey(i)) {
                        groups.get(i).add(toEvaluate.get(j));
                        groups.put(j, groups.get(i));
                    } else if(groups.containsKey(j)) {
                        groups.get(j).add(toEvaluate.get(i));
                        groups.put(i, groups.get(j));
                    } else {
                        LinkedImmunization group = new LinkedImmunization();
                        group.setType(LinkedImmunization.TYPE.SURE);
                        group.add(toEvaluate.get(i));
                        group.add(toEvaluate.get(j));
                        groups.put(i, group);
                        groups.put(j, group);
                    }
                } else if (results.get(i).get(j).equals(Result.UNSURE)) {
                    unsure.add(toEvaluate.get(j));
                } else if (results.get(i).get(j).equals(Result.DIFFERENT)) {
                    different.add(toEvaluate.get(j));
                }
            }

            if (unsure.size()>1) {
                unsures.add(unsure);
            }
            if (!lineHasEqual) {
                differents.add(different);
            }
        }

        ArrayList<LinkedImmunization> groupedImmunizations = new ArrayList<LinkedImmunization>();

        for (Integer i : groups.keySet()) {
            if (!groupedImmunizations.contains(groups.get(i)))
                groupedImmunizations.add(groups.get(i));
        }

        groupedImmunizations.addAll(unsures);
        groupedImmunizations.addAll(differents);

        return groupedImmunizations;
    }

    /**
     * Launch the deduplication process using the weighted approach
     * 
     * @param patientImmunizationRecords
     * @return
     */
    public ArrayList<LinkedImmunization> deduplicateWeighted(LinkedImmunization patientImmunizationRecords) {
        return deduplicate(patientImmunizationRecords, DeduplicationMethod.WEIGHTED);
    }

    /**
     * // Launch the deduplication process using the deterministic approach
     * @param patientImmunizationRecords
     * @return
     */
    public ArrayList<LinkedImmunization> deduplicateDeterministic(LinkedImmunization patientImmunizationRecords) {
        return deduplicate(patientImmunizationRecords, DeduplicationMethod.DETERMINISTIC);
    }

    /**
     * // Launch the deduplication process using a combination of the weighted approach and the deterministic approach
     * @param patientImmunizationRecords
     * @return
     */
    public ArrayList<LinkedImmunization> deduplicateHybrid(LinkedImmunization patientImmunizationRecords) {
        return deduplicate(patientImmunizationRecords, DeduplicationMethod.HYBRID);
    }

    /**
     * // Call the deduplication process corresponding to the specified approach
     * @param patientImmunizationRecords
     * @param method
     * @return
     */
    public ArrayList<LinkedImmunization> deduplicate(LinkedImmunization patientImmunizationRecords, DeduplicationMethod method) {
        Comparer comparer;
        switch (method) {
            case DETERMINISTIC:
                comparer = new Deterministic();
            case WEIGHTED:
                comparer = new Weighted();
            case HYBRID:
                comparer = new Hybrid();
            default :
                comparer = new Hybrid();
        }

        //immunizationNormalisation.normalizeAllImmunizations(patientImmunizationRecords);

        StepOne stepOne = new StepOne();
        StepOneResult stepOneResult = stepOne.executeStepOne(patientImmunizationRecords);
        LinkedImmunization toEvaluate = stepOneResult.getToEvaluate();

        ArrayList<ArrayList<Result>> results;

        HashMap<Integer, LinkedImmunization> groups = new HashMap<Integer, LinkedImmunization>();

        ArrayList<Result> R = new ArrayList<Result>(Collections.nCopies(toEvaluate.size(),  Result.TO_BE_DETERMINED));
        results = new ArrayList<ArrayList<Result>>(Collections.nCopies(toEvaluate.size(),  R));

        for (int i = 0; i < toEvaluate.size(); i ++) {
            for (int j = i; j < toEvaluate.size(); j ++) {
                Result result = comparer.score(toEvaluate.get(i), toEvaluate.get(j));
                results.get(i).set(j, result);
            }
        }

        return postprocessing(toEvaluate, results);
    }
}