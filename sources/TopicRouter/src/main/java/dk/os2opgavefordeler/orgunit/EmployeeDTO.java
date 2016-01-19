package dk.os2opgavefordeler.orgunit;

import javax.xml.bind.annotation.XmlElement;

public class EmployeeDTO {

    public String businessKey;
    public String name;
    public String email;
    public String initials;
    public String jobTitle;
    public String phone;
    public String esdhId;


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
