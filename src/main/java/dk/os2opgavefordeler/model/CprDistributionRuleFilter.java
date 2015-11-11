package dk.os2opgavefordeler.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class CprDistributionRuleFilter extends DistributionRuleFilter {

    /**
     * String is in format of 3-5,10-15 which means from 3 to 5 and from 10 to 15. In long form:
     * 3,4,5,10,11,12,13,14,15
     * This signifies which days of the month to filter for
     */
    private String days;

    /**
     * String is in format of 3-5,10-12 which means from 3 to 5 and from 10 to 12. In long form:
     * 3,4,5,10,11,12
     * This signifies which months in the year to filter for
     */
    private String months;

    public CprDistributionRuleFilter() {

    }

    public CprDistributionRuleFilter(String days, String months) {

    }

    private List<Integer> stringAsIntRangeList(String s) {
        List<Integer> res = new ArrayList<>();
        String[] daysArray = s.split(",");
        for (String d : daysArray) {
            // range
            if (d.contains("-")) {
                int begin = Integer.parseInt(d.split("-")[0]);
                int end = Integer.parseInt(d.split("-")[1]);
                if (end < begin) {
                    int tmp = begin;
                    begin = end;
                    end = tmp;
                }
                for (int i = begin; i <= end; i++) {
                    res.add(i);
                }
            } else {
                res.add(Integer.parseInt(d));
            }
        }
        return res;
    }

    @Override
    public boolean matches(Map<String, String> parameters) {

        if (!parameters.containsKey(getName())) {
            return false;
        }
        String param = parameters.get(getName());
        if (param.length() < 5) {
            return false;
        }

        int day = Integer.parseInt(param.substring(0, 2));
        int month = Integer.parseInt(param.substring(2, 4));

        if (!stringAsIntRangeList(days).contains(day)) {
            return false;
        }

        return stringAsIntRangeList(months).contains(month);
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getMonths() {
        return months;
    }

    public void setMonths(String months) {
        this.months = months;
    }
}
