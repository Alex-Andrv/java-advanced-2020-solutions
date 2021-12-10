package info.kgeorgiy.ja.Andreev.textStatistics.resources;

import java.util.ListResourceBundle;

public class UsageResourceBundle_ru extends ListResourceBundle {
    private final Object[][] CONTENTS = {
            {"SUMMARY_STATISTICS", "Сводная статистика"},

            {"SENTENCES_STATISTIC", "Статистика по предложениям"},
            {"SENTENCES_STATISTIC_cnt", "Число предложений: {0, number, integer} ({1, number, integer} различных)"},
            {"SENTENCES_STATISTIC_max_value", "Максимальное предложение: \"{0}\""},
            {"SENTENCES_STATISTIC_min_value", "Минимальное предложение: \"{0}\""},
            {"SENTENCES_STATISTIC_mean_value", "Не поддерживается"},
            {"SENTENCES_STATISTIC_max_len_string", "Максимальная длина предложения: {0, number, integer} (\"{1}\")"},
            {"SENTENCES_STATISTIC_min_len_string", "Минимальная длина предложения: {0, number, integer} (\"{1}\")"},
            {"SENTENCES_STATISTIC_mean_len", "Средняя длина предложения: {0, number}"},

            {"WORDS_STATISTIC", "Статистика по словам"},
            {"WORDS_STATISTIC_cnt", "Число слов: {0, number, integer} ({1, number, integer} различных)"},
            {"WORDS_STATISTIC_max_value", "Максимальное слово: \"{0}\""},
            {"WORDS_STATISTIC_min_value", "Минимальное слово: \"{0}\""},
            {"WORDS_STATISTIC_mean_value", "Не поддерживается"},
            {"WORDS_STATISTIC_max_len_string", "Максимальная длина слова: {0, number, integer} (\"{1}\")"},
            {"WORDS_STATISTIC_min_len_string", "Минимальная длина слова: {0, number, integer} (\"{1}\")"},
            {"WORDS_STATISTIC_mean_len", "Средняя длина слова: {0, number}"},

            {"NUMBER_STATISTIC", "Статистика по числам"},
            {"NUMBER_STATISTIC_cnt", "Число чисел: {0, number, integer} ({1, number, integer} различных)"},
            {"NUMBER_STATISTIC_max_value", "Максимальное число: {0}"},
            {"NUMBER_STATISTIC_min_value", "Минимальное число: {0}"},
            {"NUMBER_STATISTIC_mean_value", "Среднее число: {0, number}"},
            {"NUMBER_STATISTIC_max_len_string", "Не поддерживается"},
            {"NUMBER_STATISTIC_min_len_string", "Не поддерживается"},
            {"NUMBER_STATISTIC_mean_len", "Не поддерживается"},

            {"CURRENCY_STATISTIC", "Статистика по суммам денег"},
            {"CURRENCY_STATISTIC_cnt", "Число сумм: {0, number, integer} ({1, number, integer} различных)"},
            {"CURRENCY_STATISTIC_max_value", "Максимальная сумма: {0}"},
            {"CURRENCY_STATISTIC_min_value", "Минимальная сумма {0}"},
            {"CURRENCY_STATISTIC_mean_value", "Средняя сумма: {0, number}"},
            {"CURRENCY_STATISTIC_max_len_string", "Не поддерживается"},
            {"CURRENCY_STATISTIC_min_len_string", "Не поддерживается"},
            {"CURRENCY_STATISTIC_mean_len", "Не поддерживается"},

            {"DATA_STATISTIC", "Статистика по датам"},
            {"DATA_STATISTIC_cnt", "Число дат:  {0, number, integer} ({1, number, integer} различных)"},
            {"DATA_STATISTIC_max_value", "Максимальная дата: {0, date}"},
            {"DATA_STATISTIC_min_value", "Минимальная дата: {0, date}"},
            {"DATA_STATISTIC_mean_value", "Средняя дата: {0, date}"},
            {"DATA_STATISTIC_max_len_string", "Не поддерживается"},
            {"DATA_STATISTIC_min_len_string", "Не поддерживается"},
            {"DATA_STATISTIC_mean_len", "Не поддерживается"},

    };

    protected Object[][] getContents() {
        return CONTENTS;
    }
}
