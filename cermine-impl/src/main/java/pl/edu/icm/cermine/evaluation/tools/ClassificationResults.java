package pl.edu.icm.cermine.evaluation.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import pl.edu.icm.cermine.evaluation.AbstractEvaluator;
import pl.edu.icm.cermine.evaluation.AbstractEvaluator.Results;
import pl.edu.icm.cermine.metadata.zoneclassification.tools.LabelPair;
import pl.edu.icm.cermine.structure.model.BxZoneLabel;

public class ClassificationResults implements AbstractEvaluator.Results<ClassificationResults> {

    private List<BxZoneLabel> possibleLabels;
    private Map<LabelPair, Integer> classificationMatrix;
    private int goodRecognitions = 0;
    private int badRecognitions = 0;

    public ClassificationResults() {
        possibleLabels = new ArrayList<BxZoneLabel>();
        classificationMatrix = new HashMap<LabelPair, Integer>();
    }

    private void addPossibleLabel(BxZoneLabel lab) {
        if (possibleLabels.contains(lab)) {
            return;
        } else {
            for (BxZoneLabel lab2 : possibleLabels) {
                classificationMatrix.put(new LabelPair(lab, lab2), 0);
                classificationMatrix.put(new LabelPair(lab2, lab), 0);
            }
            classificationMatrix.put(new LabelPair(lab, lab), 0);
            possibleLabels.add(lab);
        }
    }

    public List<BxZoneLabel> getPossibleLabels() {
        return possibleLabels;
    }

    public void addOneZoneResult(BxZoneLabel label1, BxZoneLabel label2) {
        addPossibleLabel(label1);
        addPossibleLabel(label2);

        LabelPair coord = new LabelPair(label1, label2);
        classificationMatrix.put(coord, classificationMatrix.get(coord) + 1);
        if (label1.equals(label2)) {
            goodRecognitions++;
        } else {
            badRecognitions++;
        }
    }

    @Override
    public void add(ClassificationResults results) {
        for (BxZoneLabel possibleLabel : results.getPossibleLabels()) {
            addPossibleLabel(possibleLabel);
        }
        for (BxZoneLabel label1 : results.possibleLabels) {
            for (BxZoneLabel label2 : results.possibleLabels) {
                LabelPair coord = new LabelPair(label1, label2);
                classificationMatrix.put(coord, classificationMatrix.get(coord) + results.classificationMatrix.get(coord));
            }
        }
        goodRecognitions += results.goodRecognitions;
        badRecognitions += results.badRecognitions;
    }

    public Double sum(Collection<Double> collection) {
    	Double sum = 0.0;
    	for(Double elem: collection) {
    		sum += elem;
    	}
    	return sum;
    }
    
    public void printQualityMeasures() {
    	double accuracy;
    	int correctly = 0;
    	int all = 0;
    	final Double EPS = 0.00001;

    	Map<BxZoneLabel, Double> classAccuracies = new HashMap<BxZoneLabel, Double>();
    	Map<BxZoneLabel, Double> classPrecisions = new HashMap<BxZoneLabel, Double>();
    	Map<BxZoneLabel, Double> classRecalls = new HashMap<BxZoneLabel, Double>();
    	
    	for(BxZoneLabel label : possibleLabels) {
    		LabelPair positiveCoord = new LabelPair(label, label);
    		correctly += classificationMatrix.get(positiveCoord);
    		for(BxZoneLabel label1 : possibleLabels) {
    			LabelPair traversingCoord = new LabelPair(label, label1);
    			all += classificationMatrix.get(traversingCoord);
    		}
    	}
    	accuracy = (double) correctly / (double) all;
    	Formatter formatter = new Formatter(System.out, Locale.US);
    	formatter.format("Accuracy = %2.2f\n", accuracy*100.0);

    	Map<BxZoneLabel, Double> precisions = new HashMap<BxZoneLabel, Double>();
    	Integer pairsInvolved = 0;
    	for(BxZoneLabel predictedClass : possibleLabels) {
    		Integer correctPredictions = null;
    		Integer allPredictions = 0;
    		for(BxZoneLabel realClass : possibleLabels) {
    			if(realClass.equals(predictedClass)) {
    				correctPredictions = classificationMatrix.get(new LabelPair(realClass, predictedClass));
    			}
    			allPredictions += classificationMatrix.get(new LabelPair(realClass, predictedClass));
    		}
    		Double precision = (double)correctPredictions/allPredictions;
    		precisions.put(predictedClass, precision);
    		if(precision > EPS) {
    			++pairsInvolved;
    		}
    		
    	}
    	double precision = sum(precisions.values());
    	precision /= pairsInvolved;;
    	formatter.format("Precision = %2.2f\n", precision*100.0);
    	
    	Map<BxZoneLabel, Double> recalls = new HashMap<BxZoneLabel, Double>();
    	pairsInvolved = 0;
    	for(BxZoneLabel realClass : possibleLabels) {
    		Integer correctPredictions = null;
    		Integer predictions = 0;
    		for(BxZoneLabel predictedClass : possibleLabels) {
    			if(realClass.equals(predictedClass)) {
    				correctPredictions = classificationMatrix.get(new LabelPair(realClass, predictedClass));
    			}
    			predictions += classificationMatrix.get(new LabelPair(realClass, predictedClass));
    		}
    		Double recall = (double)correctPredictions/predictions;
    		recalls.put(realClass, recall);
    		if(recall > EPS) {
    			++pairsInvolved;
    		}
    	}
    	double recall = sum(recalls.values());
    	recall /= pairsInvolved;
    	formatter.format("Recall = %2.2f\n", recall*100.0);
    }
    
    public void printMatrix() {
        int maxLabelLength = 0;
        Collections.sort(possibleLabels);
        Map<BxZoneLabel, Integer> labelLengths = new HashMap<BxZoneLabel, Integer>();
        for (BxZoneLabel label : possibleLabels) {
            int labelLength = label.toString().length();
            if (labelLength > maxLabelLength) {
                maxLabelLength = labelLength;
            }
            labelLengths.put(label, labelLength);
        }

        StringBuilder oneLine = new StringBuilder();
        oneLine.append("+-").append(new String(new char[maxLabelLength]).replace('\0', '-')).append("-+");

        for (BxZoneLabel label : possibleLabels) {
            oneLine.append(new String(new char[labelLengths.get(label) + 2]).replace('\0', '-'));
            oneLine.append("+");
        }
        System.out.println(oneLine);

        oneLine = new StringBuilder();
        oneLine.append("| ").append(new String(new char[maxLabelLength]).replace('\0', ' ')).append(" |");
        for (BxZoneLabel label : possibleLabels) {
            oneLine.append(' ').append(label).append(" |");
        }
        System.out.println(oneLine);

        oneLine = new StringBuilder();
        oneLine.append("+-").append(new String(new char[maxLabelLength]).replace('\0', '-')).append("-+");
        for (BxZoneLabel label : possibleLabels) {
            oneLine.append(new String(new char[labelLengths.get(label) + 2]).replace('\0', '-'));
            oneLine.append("+");
        }
        System.out.println(oneLine);

        for (BxZoneLabel label1 : possibleLabels) {
            oneLine = new StringBuilder();
            oneLine.append("| ").append(label1);
            oneLine.append(new String(new char[maxLabelLength - labelLengths.get(label1)]).replace('\0', ' '));
            oneLine.append(" |");
            for (BxZoneLabel label2 : possibleLabels) {
                LabelPair coord = new LabelPair(label1, label2);
                String nbRecognitions = classificationMatrix.get(coord).toString();
                oneLine.append(" ").append(nbRecognitions);
                oneLine.append(new String(new char[Math.max(0, labelLengths.get(label2) - nbRecognitions.length() + 1)]).replace('\0', ' '));
                oneLine.append("|");
            }
            System.out.println(oneLine);
        }

        oneLine = new StringBuilder();
        oneLine.append("+-").append(new String(new char[maxLabelLength]).replace('\0', '-')).append("-+");
        for (BxZoneLabel label : possibleLabels) {
            oneLine.append(new String(new char[labelLengths.get(label) + 2]).replace('\0', '-'));
            oneLine.append("+");
        }
        System.out.println(oneLine);
        System.out.println();
    }

    public void printShortSummary() {
        int allRecognitions = goodRecognitions + badRecognitions;
        System.out.print("Good recognitions: " + goodRecognitions + "/" + allRecognitions);
        if (allRecognitions > 0) {
            System.out.format(" (%.1f%%)%n", 100.0 * goodRecognitions / allRecognitions);
        }
        System.out.print("Bad recognitions: " + badRecognitions + "/" + allRecognitions);
        if (allRecognitions > 0) {
            System.out.format(" (%.1f%%)%n", 100.0 * badRecognitions / allRecognitions);
        }
    }

    public void printLongSummary() {
        int maxLabelLength = 0;
        for (BxZoneLabel label : possibleLabels) {
            int labelLength = label.toString().length();
            if (labelLength > maxLabelLength) {
                maxLabelLength = labelLength;
            }
        }

        System.out.println("Good recognitions per zone type:");
        for (BxZoneLabel label1 : possibleLabels) {
            String spaces;
        	int falsePositives = 0;
            int truePositives = 0;
            int falseNegatives = 0;
            for (BxZoneLabel label2 : possibleLabels) {
                LabelPair coord = new LabelPair(label1, label2);
                if(!label1.equals(label2)) { // false positives
                	falsePositives += classificationMatrix.get(new LabelPair(label2, label1));
                }
                if (label1.equals(label2)) { //
                    truePositives += classificationMatrix.get(coord);
                } else {
                	falseNegatives += classificationMatrix.get(coord);
                }
            }

            double precision = 100.0 * (double) truePositives/(falsePositives + truePositives);
            double recall = 100.0 * truePositives / (truePositives+falseNegatives);
            spaces = new String(new char[maxLabelLength - label1.toString().length() + 1]).replace('\0', ' ');
            System.out.format("%s:%s%d/%d Pr=%4.2f Rcl=%4.2f", label1, spaces, truePositives, truePositives+falseNegatives, precision, recall);
            System.out.println();
        }
        System.out.println();
    }
}