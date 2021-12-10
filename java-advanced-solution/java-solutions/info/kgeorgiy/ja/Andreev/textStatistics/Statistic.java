package info.kgeorgiy.ja.Andreev.textStatistics;

import java.util.*;

public class Statistic {
    private final String nameStatistic;

    private final Map<String, Object[]> records;

    public Statistic(String nameStatistic) {
        this.nameStatistic = nameStatistic;
        records = new LinkedHashMap<>();
    }

    public String getNameStatistic() {
        return nameStatistic;
    }

    public Object[] getCnt() {
        return records.get("_cnt");
    }

    // :NOTE: use int instead
    public Integer getIntCnt() {
        return (Integer) getCnt()[0];
    }

    public Integer getIntDiffCnt() {
        return (Integer) getCnt()[1];
    }

    public void setAllCnt(Integer cnt, Integer cntDiff) {
        Object[] args = new Object[2];
        args[0] = cnt;
        args[1] = cntDiff;
        records.put("_cnt", args);
    }


    public void setMaxValue(Object maxValue) {
        records.put("_max_value", new Object[]{maxValue});
    }

    public Object getMaxValue() {
        return records.get("_max_value")[0];
    }

    public void setMinValue(Object minValue) {
        records.put("_min_value", new Object[]{minValue});
    }

    public Object getMinValue() {
        return records.get("_min_value")[0];
    }

    public Object getMeanValue() {
        return records.get("_mean_value")[0];
    }

    public void setMeanValue(Object meanValue) {
        records.put("_mean_value", new Object[]{meanValue});
    }

    public void setMaxLenString(String maxLenString) {
        if (!Objects.isNull(maxLenString)) {
            records.put("_max_len_string", new Object[]{maxLenString.length(),
                    maxLenString});
        }
    }


    public void setMinLenString(String minLenString) {
        if (!Objects.isNull(minLenString)) {
            records.put("_min_len_string", new Object[]{minLenString.length(),
                    minLenString});
        }
    }

    public void setMeanLen(Double meanLen) {
        records.put("_mean_len", new Object[]{meanLen});
    }

    public Map<String, Object[]> getRecords() {
        return records;
    }
}