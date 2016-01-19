package dk.os2opgavefordeler.orgunit;

import java.util.ArrayList;
import java.util.List;

public class OrgUnitDTO {

    public String businessKey;
    public String name;
    public String email;
    public String phone;
    public String esdhId;
    public String esdhLabel;
    public EmployeeDTO manager;
    public List<EmployeeDTO> employees = new ArrayList<>();
    public List<OrgUnitDTO> children = new ArrayList<>();

    public OrgUnitDTO() {

    }

    public OrgUnitDTO(String businessKey) {
        this.businessKey = businessKey;
    }


    @Override
    public String toString() {
        return "OrgUnitDTO{" +
                "businessKey='" + businessKey + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", esdhId='" + esdhId + '\'' +
                ", esdhLabel='" + esdhLabel + '\'' +
                ", manager=" + manager +
                ", employees=" + employees +
                ", children=" + children +
                '}';
    }
}
