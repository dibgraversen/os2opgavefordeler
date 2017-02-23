package dk.os2opgavefordeler.orgunit;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.OrgUnit;

import java.util.ArrayList;
import java.util.List;

public class OrgUnitDTO {

    public String businessKey;
    public String name;
    public String email;
    public String phone;
    public String pNumber;
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

    public OrgUnitDTO(OrgUnit fromOrgUnit){
        if(fromOrgUnit != null){
            this.businessKey = fromOrgUnit.getBusinessKey();
            this.name = fromOrgUnit.getName();
            this.email = fromOrgUnit.getEmail();
            this.phone = fromOrgUnit.getPhone();
            this.pNumber = fromOrgUnit.getpNumber();
            this.esdhId = fromOrgUnit.getEsdhId();
            this.esdhLabel = fromOrgUnit.getEsdhLabel();

            Employment manager = fromOrgUnit.getManager().orElse(null);
            this.manager = manager == null ? null : new EmployeeDTO(manager);

            for (Employment empl : fromOrgUnit.getEmployees()) {
                employees.add(new EmployeeDTO(empl));
            }

            for (OrgUnit orgUnit : fromOrgUnit.getChildren()){
                children.add(new OrgUnitDTO(orgUnit));
            }
        }
    }


    @Override
    public String toString() {
        return "OrgUnitDTO{" +
                "businessKey='" + businessKey + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", pNumber='" + pNumber + '\'' +
                ", esdhId='" + esdhId + '\'' +
                ", esdhLabel='" + esdhLabel + '\'' +
                ", manager=" + manager +
                ", employees=" + employees +
                ", children=" + children +
                '}';
    }
}
