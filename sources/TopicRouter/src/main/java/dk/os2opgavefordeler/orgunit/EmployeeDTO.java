package dk.os2opgavefordeler.orgunit;

import dk.os2opgavefordeler.model.Employment;

import javax.xml.bind.annotation.XmlElement;

public class EmployeeDTO {

    public String businessKey;
    public String name;
    public String email;
    public String initials;
    public String jobTitle;
    public String phone;
    public String esdhId;

    public EmployeeDTO(){}

    public EmployeeDTO(Employment fromEmployment){
        if (fromEmployment != null) {
            businessKey = fromEmployment.getBusinessKey();
            name = fromEmployment.getName();
            email = fromEmployment.getEmail();
            initials = fromEmployment.getInitials();
            jobTitle = fromEmployment.getJobTitle();
            phone = fromEmployment.getPhone();
            esdhId = fromEmployment.getEsdhId();
        }
    }

    @Override
    public String toString() {
        return "EmployeeDTO{" +
                "businessKey='" + businessKey + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", initials='" + initials + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", phone='" + phone + '\'' +
                ", esdhId='" + esdhId + '\'' +
                '}';
    }
}
