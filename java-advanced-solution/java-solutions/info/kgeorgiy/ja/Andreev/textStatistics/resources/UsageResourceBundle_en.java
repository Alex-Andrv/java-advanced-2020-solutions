package info.kgeorgiy.ja.Andreev.textStatistics.resources;

import java.util.ListResourceBundle;

public class UsageResourceBundle_en extends ListResourceBundle {
    private final Object[][] CONTENTS = {
            {"SUMMARY_STATISTICS", "Summary statistics"},

            {"SENTENCES_STATISTIC", "Sentence statistics"},
            {"SENTENCES_STATISTIC_cnt", "Number of offers: {0, number, integer} ({1, number, integer} different)"},
            {"SENTENCES_STATISTIC_max_value", "Maximum sentence: \"{0}\""},
            {"SENTENCES_STATISTIC_min_value", "Minimum sentence: \"{0}\""},
            {"SENTENCES_STATISTIC_mean_value", "Not supported"},
            {"SENTENCES_STATISTIC_max_len_string", "Maximum sentence length: {0, number, integer} (\"{1}\")"},
            {"SENTENCES_STATISTIC_min_len_string", "Minimum sentence length: {0, number, integer} (\"{1}\")"},
            {"SENTENCES_STATISTIC_mean_len", "Average sentence length: {0, number}"},

            {"WORDS_STATISTIC", "Word statistics"},
            {"WORDS_STATISTIC_cnt", "Number of words: {0, number, integer} ({1, number, integer} different)"},
            {"WORDS_STATISTIC_max_value", "Maximum word: \"{0}\""},
            {"WORDS_STATISTIC_min_value", "Minimum word: \"{0}\""},
            {"WORDS_STATISTIC_mean_value", "Not supported"},
            {"WORDS_STATISTIC_max_len_string", "Maximum word length: {0, number, integer} (\"{1}\")"},
            {"WORDS_STATISTIC_min_len_string", "Minimum word length: {0, number, integer} (\"{1}\")"},
            {"WORDS_STATISTIC_mean_len", "Average word length: {0, number}"},

            {"NUMBER_STATISTIC", "Statistics by numbers"},
            {"NUMBER_STATISTIC_cnt", "Number of numbers: {0, number, integer} ({1, number, integer} different)"},
            {"NUMBER_STATISTIC_max_value", "Maximum number: {0}"},
            {"NUMBER_STATISTIC_min_value", "Minimum number: {0}"},
            {"NUMBER_STATISTIC_mean_value", "Average number: {0, number}"},
            {"NUMBER_STATISTIC_max_len_string", "Not supported"},
            {"NUMBER_STATISTIC_min_len_string", "Not supported"},
            {"NUMBER_STATISTIC_mean_len", "Not supported"},

            {"CURRENCY_STATISTIC", "Amount of money statistics"},
            {"CURRENCY_STATISTIC_cnt", "Number of Sums: {0, number, integer} ({1, number, integer} different)"},
            {"CURRENCY_STATISTIC_max_value", "Maximum amount: {0}"},
            {"CURRENCY_STATISTIC_min_value", "Minimum amount: {0}"},
            {"CURRENCY_STATISTIC_mean_value", "Average sum: {0, number}"},
            {"CURRENCY_STATISTIC_max_len_string", "Not supported"},
            {"CURRENCY_STATISTIC_min_len_string", "Not supported"},
            {"CURRENCY_STATISTIC_mean_len", "Not supported"},

            {"DATA_STATISTIC", "Date statistics"},
            {"DATA_STATISTIC_cnt", "Number of dates:  {0, number, integer} ({1, number, integer} different)"},
            {"DATA_STATISTIC_max_value", "Maximum date: {0, date}"},
            {"DATA_STATISTIC_min_value", "Minimum date: {0, date}"},
            {"DATA_STATISTIC_mean_value", "Average date: {0, date}"},
            {"DATA_STATISTIC_max_len_string", "Not supported"},
            {"DATA_STATISTIC_min_len_string", "Not supported"},
            {"DATA_STATISTIC_mean_len", "Not supported"},

    };

    protected Object[][] getContents() {
        return CONTENTS;
    }
}
