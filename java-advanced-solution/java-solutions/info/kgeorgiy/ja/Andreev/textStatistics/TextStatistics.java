package info.kgeorgiy.ja.Andreev.textStatistics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.*;
import java.util.*;
import java.util.function.Function;


public class TextStatistics {

    public static final Charset FILE_CHARSET = StandardCharsets.UTF_8;


    private static void printUsage(String error) {
        if (!error.isEmpty()) {
            System.err.println(error);
        }
        System.err.println("Usage: <input locale> <output locale> <input file> <report file>");
    }

    private static void printAvailableLocales() {
        for (Locale locale : Locale.getAvailableLocales()) {
            System.err.println(locale);
        }
    }

    public static void main(String[] args) {
        Objects.requireNonNull(args);
        if (args.length != 4 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            printUsage("");
            return;
        }

        final Locale inputLocale;
        final Locale outputLocale;

        try {
            inputLocale = getLocale(args[0]);
            outputLocale = getLocale(args[1]);
        } catch (NoSuchElementException e) {
            printUsage("Invalid input/output language. See ISO-639 and ISO-3166: ");
            printAvailableLocales();
            return;
        }

        if (!(outputLocale.getLanguage().equals("ru") || outputLocale.getLanguage().equals("en"))) {
            printUsage("Error: Unsupported output locale provided: "
                    +  inputLocale.getDisplayName());
        }

        Path inputFile;
        Path outputFile;

        try {
            inputFile = Paths.get(args[2]);
            outputFile = Paths.get(args[3]);
        } catch (InvalidPathException e) {
            System.err.println("Invalid input/output File name \n" + e);
            return;
        }

        final String source;

        try {
            source = Files.readString(inputFile, FILE_CHARSET);
        } catch (IOException e) {
            System.err.println("Can't read file: " + inputFile.toAbsolutePath());
            return;
        }

        Result result = new TextStatistics().handler(source, inputLocale);

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, FILE_CHARSET)) {
            writer.write(result.toString(outputLocale));
        } catch (IOException e) {
            System.err.println("Can't write data to file: " + outputFile.toAbsolutePath());
        }
    }

    public Result handler(final String source, final Locale locale) {
        Result result = new Result();
        result.add(sentencesHandler(source, locale));
        result.add(wordsHandler(source, locale));
        result.add(numberHandler(source, locale));
        result.add(currencyHandler(source, locale));
        result.add(dataHandler(source, locale));
        return result;
    }

    public Statistic sentencesHandler(final String source, final Locale locale) {
        TreeMap<CollationKey, Integer> sentences = getSentences(source, locale);
        Statistic sentencesStatistic = getStatistic("SENTENCES_STATISTIC",
                sentences, CollationKey::getSourceString);
        findMaxMinLen(sentencesStatistic, sentences);
        return sentencesStatistic;
    }

    public Statistic wordsHandler(final String source, final Locale locale) {
        TreeMap<CollationKey, Integer> words = getWords(source, locale);
        Statistic wordsStatistic = getStatistic("WORDS_STATISTIC",
                words, CollationKey::getSourceString);
        findMaxMinLen(wordsStatistic, words);
        return wordsStatistic;
    }

    private void findMaxMinLen(Statistic statistic, TreeMap<CollationKey, Integer> data) {
        statistic.setMaxLenString(data.keySet().stream().map(CollationKey::getSourceString).
                max(Comparator.comparingLong(String::length)).orElse(null));
        statistic.setMinLenString(data.keySet().stream().map(CollationKey::getSourceString).
                min(Comparator.comparingLong(String::length)).orElse(null));
        if (statistic.getIntCnt() != 0) {
            statistic.setMeanLen(data.entrySet().stream().
                    map((entry) -> entry.getKey().getSourceString().length() * entry.getValue()).reduce(0, Integer::sum).doubleValue()
                    / statistic.getIntCnt());
        }
    }

    public Statistic numberHandler(final String source, final Locale locale) {
        TreeMap<Double, Integer> numbers = getNumber(source, locale);
        Statistic numberStatistic = getStatistic("NUMBER_STATISTIC",
                numbers, Function.identity());
        if (numberStatistic.getIntCnt() != 0) {
            numberStatistic.setMeanValue(numbers.entrySet().stream().
                    map((entry) -> entry.getKey() * entry.getValue()).reduce(0., Double::sum)
                    / numberStatistic.getIntCnt());
        }
        return numberStatistic;
    }

    public Statistic currencyHandler(final String source, final Locale locale) {
        TreeMap<Double, Integer> currencies = getCurrency(source, locale);
        Statistic currencyStatistic = getStatistic("CURRENCY_STATISTIC",
                currencies, Function.identity());
        if (currencyStatistic.getIntCnt() != 0) {
            currencyStatistic.setMeanValue(currencies.entrySet().stream().
                    map((entry) -> entry.getKey() * entry.getValue()).reduce(0., Double::sum)
                    / currencyStatistic.getIntCnt());
        }
        return currencyStatistic;
    }

    public Statistic dataHandler(final String source, final Locale locale) {
        TreeMap<Date, Integer> dates = getData(source, locale);
        Statistic dataStatistic = getStatistic("DATA_STATISTIC",
                dates, Function.identity());
        if (dataStatistic.getIntCnt() != 0) {
            dataStatistic.setMeanValue(new Date(dates.entrySet().stream().
                    map((entry) -> entry.getKey().getTime() * entry.getValue()).reduce(0L, Long::sum)
                    / dataStatistic.getIntCnt()));
        }
        return dataStatistic;
    }

    private TreeMap<CollationKey, Integer> getSentences(final String source, final Locale locale) {
        return split(source, BreakIterator.getSentenceInstance(locale), locale);
    }

    private TreeMap<CollationKey, Integer> getWords(final String source, final Locale locale) {
        return split(source, BreakIterator.getWordInstance(locale), locale);
    }

    private TreeMap<Double, Integer> getNumber(final String source, final Locale locale) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        return parseNumber(numberFormat, source);

    }

    private TreeMap<Double, Integer> getCurrency(final String source, final Locale locale) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
        return parseNumber(numberFormat, source);
    }

    private TreeMap<Double, Integer> parseNumber(NumberFormat format, final String source) {
        TreeMap<Double, Integer> numbers = new TreeMap<>( );
        ParsePosition startPosition = new ParsePosition(0);
        while (startPosition.getIndex() < source.length()) {
            ParsePosition endPosition = new ParsePosition(startPosition.getIndex());
            Number number = format.parse(source, endPosition);
            if (!Objects.isNull(number)) {
                numbers.merge(number.doubleValue(), 1, Integer::sum);
                startPosition = endPosition;
            } else {
                startPosition.setIndex(startPosition.getIndex() + 1);
            }
        }
        return numbers;
    }

    private TreeMap<Date, Integer> getData(final String source, final Locale locale) {
        List<DateFormat> dateFormats = List.of(DateFormat.getDateInstance(DateFormat.FULL, locale),
                DateFormat.getDateInstance(DateFormat.LONG, locale),
                DateFormat.getDateInstance(DateFormat.MEDIUM, locale),
                DateFormat.getDateInstance(DateFormat.SHORT, locale));
        TreeMap<Date, Integer> dates = new TreeMap<>( );
        ParsePosition startPosition = new ParsePosition(0);
        while (startPosition.getIndex() < source.length()) {
            ParsePosition endPosition = new ParsePosition(startPosition.getIndex());
            Date date = null;
            for (DateFormat dateFormat : dateFormats) {
                date = dateFormat.parse(source, endPosition);
                if (!Objects.isNull(date)) {
                    break;
                }
            }
            if (!Objects.isNull(date)) {
                dates.merge(date, 1, Integer::sum);
                startPosition = endPosition;
            } else {
                startPosition.setIndex(startPosition.getIndex() + 1);
            }
        }
        return dates;
    }



    private <T, U> Statistic getStatistic(String nameStatistic,
                                             TreeMap<U, Integer> currentMap,
                                             Function<U, ? extends T> convertor) {
        Statistic statistic = new Statistic(nameStatistic);
        Integer cnt = currentMap.values().stream().reduce(0, Integer::sum);
        statistic.setAllCnt(cnt, currentMap.size());
        if (cnt != 0) {
            statistic.setMinValue(convertor.apply(currentMap.firstKey()));
            statistic.setMaxValue(convertor.apply(currentMap.lastKey()));
        } else {
            statistic.setMinValue(0d);
            statistic.setMaxValue(0d);
        }
        return statistic;
    }

    public static TreeMap<CollationKey, Integer> split(final String text, final BreakIterator boundary, final Locale locale) {
        boundary.setText(text);
        Collator myCollator = Collator.getInstance(locale);
        myCollator.setDecomposition(Collator.PRIMARY);
        final TreeMap<CollationKey, Integer> parts = new TreeMap<>();
        for (
                int begin = boundary.first(), end = boundary.next();
                end != BreakIterator.DONE;
                begin = end, end = boundary.next()
        ) {
            String part = text.substring(begin, end).trim();
            if (part.codePoints().anyMatch(Character::isLetter)) {
                CollationKey collationKey = myCollator.getCollationKey(part);
                parts.merge(collationKey, 1, Integer::sum);
            }
        }
        return parts;
    }

    private static Locale getLocale(String language) {
        return Arrays.stream(Locale.getAvailableLocales()).filter(locale ->
                locale.toString().contains(language)).
                min(Comparator.comparingInt((locale) -> locale.toString().length())).orElseThrow();
        //Arrays.stream(Locale.getAvailableLocales()).map(Locale::toString).filter(locale -> locale.startsWith("ru")).sorted().collect(Collectors.toList())
    }


}
