package dk.os2opgavefordeler.model;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.util.Map;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class TextDistributionRuleFilter extends DistributionRuleFilter {

    private static final String URL_PARAMETER_NAME = "text";

    /**
     * Uses * as wildcard. Wildcards can be placed at beginning and end, not in the middle.
     */
    private String text;

    public TextDistributionRuleFilter() {

    }

    public TextDistributionRuleFilter(String name, DistributionRule distributionRule, OrgUnit orgUnit, Employment employment, String text) {
        super(name, distributionRule, orgUnit, employment);
        this.text = text;
    }

    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }

    @Override
    public boolean matches(Map<String, String> parameters) {
	    if (!parameters.containsKey(URL_PARAMETER_NAME)) {
		    return false;
	    }

        String parameter = parameters.get(URL_PARAMETER_NAME);

        if (parameter == null || parameter.isEmpty()) {
            return false;
        }

        if (text.startsWith("*") && text.endsWith("*")) {
            String s = StringUtils.removeEnd(text, "*");
            s = StringUtils.removeStart(s, "*");

            return parameter.contains(s);
        }

        if (text.startsWith("*")) {
            String s = StringUtils.removeStart(text, "*");

            return parameter.endsWith(s);
        }

        if (text.endsWith("*")) {
            String s = StringUtils.removeEnd(text, "*");

            return parameter.startsWith(s);
        }

        return parameter.equals(text);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {

        this.text = text;
    }
}
