package org.immregistries.vaccination_deduplication;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.immregistries.vaccination_deduplication.computation_classes.Comparer;
import org.immregistries.vaccination_deduplication.computation_classes.Deterministic;
import org.immregistries.vaccination_deduplication.computation_classes.Hybrid;
import org.immregistries.vaccination_deduplication.computation_classes.StepOne;
import org.immregistries.vaccination_deduplication.computation_classes.Weighted;
import org.immregistries.vaccination_deduplication.utils.ImmunizationNormalisation;

/**
 * This class is the entry point for the API. It contains the main logic for the deduplication process.
 */
public class VaccinationDeduplication {
    static VaccinationDeduplication instance;

    PropertyLoader propertyLoader;
    ImmunizationNormalisation immunizationNormalisation;

    private VaccinationDeduplication() {
        this.propertyLoader = new PropertyLoader();
        this.immunizationNormalisation = ImmunizationNormalisation.getInstance();
    }

    /**
     * This method will return the singleton instance of VaccinationDeduplication
     * @return The singleton instance
     */
    public static VaccinationDeduplication getInstance() {
        if (instance == null)
            instance = new VaccinationDeduplication();
        return instance;
    }

    /**
     * This method (or initialize()) has to be called after getting the instance for the first time.
     * It will initialize the class for the Immunization Normalisation process.
     * It will use the codebase file at the given path.
     *
     * @param codebaseFilePath The path to the codebase file.
     * @throws FileNotFoundException Throws exception if the codebase file is not found.
     */
    public void initialize(String codebaseFilePath) throws FileNotFoundException {
        this.immunizationNormalisation.initialize(codebaseFilePath);
    }

    /**
     * This method (or initialize(String codebaseFilePath)) has to be called after getting the instance for the first time.
     * It will initialize the class for the Immunization Normalisation process.
     * It will use the codebase file present in the codebase client jar.
     */
    public void initialize() {
        this.immunizationNormalisation.initialize();
    }

    /**
     * This method will change the codebase file used by the Immunization Normalisation process.
     * @param codebaseFilePath The path to the codebase file.
     * @throws FileNotFoundException Throws exception if the codebase file is not found.
     */
    public void refreshCodebase(String codebaseFilePath) throws FileNotFoundException {
        this.immunizationNormalisation.refreshCodebase(codebaseFilePath);
    }

    /**
     * This method will process the results form the comparison process and return the LinkedImmunization grouping duplicates (or unsures or non duplicates) together.
     * @param toEvaluate This ArrayList contains the Immunizations that have been determined by step one to be potential duplicates.
     * @param results This 2D ArrayList contains the results from the comparisons of the toEvaluate Immunizations.
     * @return An ArrayList of LinkedImmunization containing the final result from the deduplication process.
     */
    public ArrayList<LinkedImmunization> postprocessing(LinkedImmunization toEvaluate, ArrayList<ArrayList<ComparisonResult>> results) {
        for (ArrayList<ComparisonResult> line : results) {
            for (ComparisonResult r : line) {
                System.out.print(r+"\t");
            }
            System.out.println();
        }

        HashMap<Integer, LinkedImmunization> sameGrouped = new HashMap<Integer, LinkedImmunization>();
        HashMap<Integer, LinkedImmunization> unsureGrouped = new HashMap<Integer, LinkedImmunization>();

        // first pass to group the ones we are SURE are the same together
        for (int i = 0; i < results.size()-1; i++) {
            for (int j = i+1; j < results.size(); j++) {
                if (results.get(i).get(j).equals(ComparisonResult.EQUAL)) {
                    if(sameGrouped.containsKey(i) && sameGrouped.containsKey(j)) {

                    } else if(sameGrouped.containsKey(i)) {
                        sameGrouped.get(i).add(toEvaluate.get(j));
                        sameGrouped.put(j, sameGrouped.get(i));
                    } else if(sameGrouped.containsKey(j)) {
                        sameGrouped.get(j).add(toEvaluate.get(i));
                        sameGrouped.put(i, sameGrouped.get(j));
                    } else {
                        LinkedImmunization group = new LinkedImmunization();
                        group.setType(LinkedImmunizationType.SURE);
                        group.add(toEvaluate.get(i));
                        group.add(toEvaluate.get(j));
                        sameGrouped.put(i, group);
                        sameGrouped.put(j, group);
                    }
                }
            }
        }

        // second pass to handle the UNSURE
        for (int i = 0; i < results.size()-1; i++) {
            if (!sameGrouped.keySet().contains(i)) {
                for (int j = i+1; j < results.size(); j++) {
                    if (results.get(i).get(j).equals(ComparisonResult.UNSURE)) {
                        if(unsureGrouped.containsKey(i) && unsureGrouped.containsKey(j)) {

                        } else if(unsureGrouped.containsKey(i)) {
                            unsureGrouped.get(i).add(toEvaluate.get(j));
                            unsureGrouped.put(j, unsureGrouped.get(i));
                        } else if(unsureGrouped.containsKey(j)) {
                            unsureGrouped.get(j).add(toEvaluate.get(i));
                            unsureGrouped.put(i, unsureGrouped.get(j));
                        } else {
                            LinkedImmunization group = new LinkedImmunization();
                            group.setType(LinkedImmunizationType.UNSURE);
                            group.add(toEvaluate.get(i));
                            group.add(toEvaluate.get(j));
                            unsureGrouped.put(i, group);
                            unsureGrouped.put(j, group);
                        }
                    }
                }
            }
        }

        // third pass to handle the DIFFERENT
        LinkedImmunization different = new LinkedImmunization();
        different.setType(LinkedImmunizationType.DIFFERENT);

        for (int i = 0; i < results.size(); i++) {
            if (!sameGrouped.keySet().contains(i) && !unsureGrouped.keySet().contains(i)){
                different.add(toEvaluate.get(i));
            }
        }

        ArrayList<LinkedImmunization> groupedImmunizations = new ArrayList<LinkedImmunization>();

        for (Integer i : sameGrouped.keySet()) {
            if (!groupedImmunizations.contains(sameGrouped.get(i)))
                groupedImmunizations.add(sameGrouped.get(i));
        }
        for (Integer i : unsureGrouped.keySet()) {
            if (!groupedImmunizations.contains(unsureGrouped.get(i)))
                groupedImmunizations.add(unsureGrouped.get(i));
        }

        if (different.size()>0)
            groupedImmunizations.add(different);

        return groupedImmunizations;
    }

    /**
     * This method will launch the deduplication process using the weighted approach.
     * @param patientImmunizationRecords A LinkedImmunization object containing all the immunization records for a patient.
     * @return An ArrayList of LinkedImmunization containing the final result from the deduplication process.
     */
    public ArrayList<LinkedImmunization> deduplicateWeighted(LinkedImmunization patientImmunizationRecords) {
        return deduplicate(patientImmunizationRecords, DeduplicationMethod.WEIGHTED);
    }

    /**
     * This method will launch the deduplication process using the deterministic approach
     * @param patientImmunizationRecords A LinkedImmunization object containing all the immunization records for a patient.
     * @return An ArrayList of LinkedImmunization containing the final result from the deduplication process.
     */
    public ArrayList<LinkedImmunization> deduplicateDeterministic(LinkedImmunization patientImmunizationRecords) {
        return deduplicate(patientImmunizationRecords, DeduplicationMethod.DETERMINISTIC);
    }

    /**
     * This method will launch the deduplication process using a hybrid of the weighted approach and the deterministic approach.
     * @param patientImmunizationRecords A LinkedImmunization object containing all the immunization records for a patient.
     * @return An ArrayList of LinkedImmunization containing the final result from the deduplication process.
     */
    public ArrayList<LinkedImmunization> deduplicateHybrid(LinkedImmunization patientImmunizationRecords) {
        return deduplicate(patientImmunizationRecords, DeduplicationMethod.HYBRID);
    }

    /**
     * This method will call all the methods necessary to the deduplication process.
     * It can also be called directly instead of using the specific methods.
     * @param patientImmunizationRecords A LinkedImmunization object containing all the immunization records for a patient.
     * @param method The method to use for the deduplication process.
     * @return An ArrayList of LinkedImmunization containing the final result from the deduplication process.
     */
    public ArrayList<LinkedImmunization> deduplicate(LinkedImmunization patientImmunizationRecords, DeduplicationMethod method) {
        Comparer comparer;
        switch (method) {
            case DETERMINISTIC:
                comparer = new Deterministic();
                break;
            case WEIGHTED:
                comparer = new Weighted(this.propertyLoader);
                break;
            case HYBRID:
                comparer = new Hybrid();
                break;
            default :
                comparer = new Hybrid();
        }

        immunizationNormalisation.normalizeAllImmunizations(patientImmunizationRecords);

        StepOne stepOne = new StepOne();
        LinkedImmunization toEvaluate = patientImmunizationRecords;

        ArrayList<ArrayList<ComparisonResult>> results;

        results = new ArrayList<ArrayList<ComparisonResult>>(toEvaluate.size());
        for (int i = 0; i < toEvaluate.size(); i++) {
            results.add(new ArrayList<ComparisonResult>());
            for (int j = 0; j < toEvaluate.size(); j++) {
                results.get(i).add(ComparisonResult.TO_BE_DETERMINED);
            }
        }

        for (int i = 0; i < toEvaluate.size()-1; i ++) {
            for (int j = i+1; j < toEvaluate.size(); j ++) {
                if (stepOne.isPotentialDuplicate(toEvaluate.get(i), toEvaluate.get(j))) {
                    ComparisonResult result = comparer.compare(toEvaluate.get(i), toEvaluate.get(j));
                    results.get(i).set(j, result);
                    results.get(j).set(i, result);
                } else {
                    results.get(i).set(j, ComparisonResult.DIFFERENT);
                    results.get(j).set(i, ComparisonResult.DIFFERENT);
                }
            }
        }

        return postprocessing(toEvaluate, results);
    }
}