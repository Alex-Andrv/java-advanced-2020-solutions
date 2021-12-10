package info.kgeorgiy.ja.Andreev.textStatistics;

import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.*;

public class Result {

    private final static String SUMMARY_STATISTICS = "SUMMARY_STATISTICS";
    // :NOTE: \t
    private final static String TAB = "     ";
    private final static String NEW_LINE = "\n";

    private final List<Statistic> research = new ArrayList<>();

    public void add(Statistic statistic) {
        research.add(statistic);
    }



    private String getSummaryStatistics(ResourceBundle resourceBundle, Locale locale) {
        StringBuffer summaryStatistics = new StringBuffer();

        summaryStatistics.append(resourceBundle.getString(SUMMARY_STATISTICS)).
                append(NEW_LINE);

        for (Statistic statistic : research) {
            MessageFormat messageFormat = new MessageFormat(
                    resourceBundle.getString(statistic.getNameStatistic() + "_cnt"), locale);
            summaryStatistics.append(TAB);
            messageFormat.format(statistic.getCnt(), summaryStatistics, new FieldPosition(0));
            summaryStatistics.append(NEW_LINE);
        }

        return summaryStatistics.toString();
    }

    private String getStatistics(Statistic statistic, ResourceBundle resourceBundle, Locale locale) {
        StringBuffer stringStatistics = new StringBuffer();


        String nameStatistic = statistic.getNameStatistic();

        stringStatistics.append(resourceBundle.getString(nameStatistic)).
                append(NEW_LINE);

        for (Map.Entry<String, Object[]> args : statistic.getRecords().entrySet()) {
            MessageFormat messageFormat = new MessageFormat(
                    resourceBundle.getString(nameStatistic + args.getKey()), locale);
            stringStatistics.append(TAB);
            messageFormat.format(args.getValue(), stringStatistics, new FieldPosition(0));
            stringStatistics.append(NEW_LINE);
        }

        return stringStatistics.toString();

    }

    public String toString(Locale locale) {
        StringBuilder report = new StringBuilder();
        ResourceBundle resourceBundle = ResourceBundle.getBundle(this.getClass().getPackageName() +
                ".resources.UsageResourceBundle", locale);
        report.append(getSummaryStatistics(resourceBundle, locale));
        for (Statistic statistic : research) {
            report.append(getStatistics(statistic, resourceBundle, locale));
        }
        return report.toString();
    }
}
