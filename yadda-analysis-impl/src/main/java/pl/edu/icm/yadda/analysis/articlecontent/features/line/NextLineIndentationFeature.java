package pl.edu.icm.yadda.analysis.articlecontent.features.line;

import pl.edu.icm.yadda.analysis.classification.features.FeatureCalculator;
import pl.edu.icm.yadda.analysis.textr.model.BxLine;
import pl.edu.icm.yadda.analysis.textr.model.BxPage;

/**
 *
 * @author Dominika Tkaczyk (dtkaczyk@icm.edu.pl)
 */
public class NextLineIndentationFeature implements FeatureCalculator<BxLine, BxPage> {

    private static String featureName = "NextLineIndentation";

    @Override
    public String getFeatureName() {
        return featureName;
    }

    @Override
    public double calculateFeatureValue(BxLine line, BxPage page) {
        if (!line.hasNext()) {
            return 0.0;
        }
        
        int i = 0;
        BxLine l = line.getNext();
        double meanX = 0;
        while (l.hasNext() && i < 5) {
            l = l.getNext();
            if ((line.getX() < l.getX() && l.getX() < line.getX() + line.getWidth()) 
                    || (l.getX() < line.getX() && line.getX() < l.getX() + l.getWidth())) {
                meanX += l.getX();
                i++;
            } else {
                break;
            }
        }
        if (i == 0) {
            return 0.0;
        }
        
        meanX /= i;
        return Math.abs(line.getNext().getX() - meanX) / (double) line.getWidth();
    }
}