package dk.os2opgavefordeler.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.util.Map;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class TextDistributionRuleFilter extends DistributionRuleFilter {

    /**
     * Uses * as wildcard. Wildcards can be placed at beginning and end, not in the middle.
     */
    private String text;

    @Override
    public boolean matches(Map<String, String> parameters) {
        String parameter = parameters.get(getName());
        if (parameter == null) {
            return false;
        }

        if(text.startsWith("*") && text.endsWith("*")){
            return parameter.contains(text);
        }

        if(text.startsWith("*")){
            return parameter.endsWith(text);
        }

        if(text.endsWith("*")){
            return parameter.startsWith(text);
        }

        return false;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {

        this.text = text;
    }
}
