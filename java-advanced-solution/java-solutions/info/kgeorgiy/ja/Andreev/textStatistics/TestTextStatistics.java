package info.kgeorgiy.ja.Andreev.textStatistics;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class TestTextStatistics {

    private Random random = new Random();

    @Test
    public void test01_numbers() {
        TextStatistics textStatistics = new TextStatistics();
        testDouble(NumberFormat::getNumberInstance, textStatistics::numberHandler);

    }

    @Test
    public void test02_currency() {
        TextStatistics textStatistics = new TextStatistics();
        testDouble(NumberFormat::getCurrencyInstance, textStatistics::currencyHandler);
    }

    @Test
    public void test03_words() {
        TextStatistics textStatistics = new TextStatistics();
        Locale locale = new Locale("ru", "RU");
        String source = "Кто жизнью бит, тот большего добьется.\n" +
                "Пуд соли съевший выше ценит мед.\n" +
                "Кто слезы лил, тот искренней смеется.\n" +
                "Кто умирал, тот знает, что живет!";
        Statistic statistic = textStatistics.wordsHandler(source, locale);
        final Integer cnt = 24;
        String min = "бит";
        String max = "что";
        checkMainValue(statistic, cnt, min, max, String.class, Object::equals);
    }


    @Test
    public void test04_wordsWithNumbers() {
        TextStatistics textStatistics = new TextStatistics();
        Locale locale = new Locale("ru", "RU");
        String source = "Приве, всем. Ян. 43. 477774. что-то.";
        Statistic statistic = textStatistics.wordsHandler(source, locale);
        final Integer cnt = 4;
        String min = "всем";
        String max = "Ян";
        checkMainValue(statistic, cnt, min, max, String.class, Object::equals);
    }

    @Test
    public void test05_wordsWithNumbers() {
        TextStatistics textStatistics = new TextStatistics();
        Locale locale = new Locale("ru", "KZ");
        String source = "Жаздыгїн шілде болєанда,\n" +
                "Кґкорай шалєын, бјйшешек,\n" +
                "Ўзарып ґсіп толєанда. \n" +
                "Кїркіреп жатќан ґзенге,\n" +
                "Кґшіп ауыл ќонєанда.\n" +
                "Шўрќырап жатќан жылќыныѕ\n" + "07.06.2021";
        Statistic statistic = textStatistics.wordsHandler(source, locale);
        final Integer cnt = 18;
        String min = "ауыл";
        String max = "Шўрќырап";
        checkMainValue(statistic, cnt, min, max, String.class, Object::equals);
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of("output.txt"),  StandardCharsets.UTF_8)) {
            writer.write(textStatistics.handler(source, locale).toString(new Locale("ru", "RU")));
        } catch (IOException e) {
            System.err.println("Can't write data to file: ");
        }


    }



    private void testDouble(Function<Locale, NumberFormat> getNumberFormat,
                            BiFunction<String, Locale, Statistic> getStatistics) {
        for (Locale locale : Locale.getAvailableLocales()) {
            NumberFormat format = getNumberFormat.apply(locale);
            StringBuilder source = new StringBuilder();
            Integer cnt = random.nextInt(5) + 1;
            Double max = null;
            Double min = null;
            Double sum = 0d;
            for (int i = 0; i < cnt; i++) {
                Double number = random.nextDouble() * 1000;
                if (Objects.isNull(max))
                    max = number;
                if (Objects.isNull(min)) {
                    min = number;
                }
                number = Math.round(number * 10000) / 10000.;
                max = Double.max(max, number);
                min = Double.min(min, number);
                sum += number;
                source.append(format.format(number)).append(" ");
            }
            Double average = sum / cnt;
            Statistic statistic = getStatistics.apply(source.toString(), locale);
            checkMainValue(statistic, cnt, min, max, Double.class, (f, s) -> Math.abs((Double)f - (Double) s) < 1);
            Assert.assertNotNull(statistic.getMeanValue());
            Assert.assertTrue(statistic.getMeanValue().getClass().isAssignableFrom(Double.class));
            Assert.assertEquals(average, (Double) statistic.getMeanValue(), 1);
        }
    }

    private void checkMainValue(Statistic statistic, Integer cnt, Object min, Object max,
                                    Class<?> expectedClass, BiPredicate<Object, Object> equals) {
        Assert.assertNotNull(statistic);
        Assert.assertNotNull(statistic.getNameStatistic());
        Assert.assertNotNull(statistic.getIntCnt());
        Assert.assertEquals(cnt, statistic.getIntCnt());
        Assert.assertNotNull(statistic.getMinValue());
        Assert.assertTrue(statistic.getMinValue().getClass().isAssignableFrom(expectedClass));
        Assert.assertTrue(equals.test(min, statistic.getMinValue()));
        Assert.assertNotNull(statistic.getMaxValue());
        Assert.assertTrue(statistic.getMaxValue().getClass().isAssignableFrom(expectedClass));
        Assert.assertTrue(equals.test(max, statistic.getMaxValue()));
    }

}
