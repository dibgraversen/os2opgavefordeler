package dk.os2opgavefordeler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.json.Json;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.text.SimpleDateFormat;

import java.util.Date;

/**
 * Log entry describing a system event like a deletion of a record
 */
@Entity
@Table(name = "auditlog")
public class LogEntry {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name="eventtime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeStamp;

    @Transient
    private String friendlyTimeStamp;

    @Column(name="kle")
    private String kle;

    @Column(name="username")
    private String user;

    @Column(name="operation")
    private String operation;

    @Column(name="eventtype")
    private String type;

    @Column(name="eventdata")
    private String data;

    @Column(name = "orgunit")
    private String orgUnit;

    @Column(name = "employment")
    private String employment;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JsonIgnore
    private Municipality municipality;

    // operations
    public static final String CREATE_TYPE = "Opret";
    public static final String DELETE_TYPE = "Slet";
    public static final String UPDATE_TYPE = "Rediger";

    // types
    public static final String RESPONSIBILITY_TYPE = "Ansvar";
    public static final String DISTRIBUTION_TYPE = "Fordeling";
    public static final String EXTENDED_DISTRIBUTION_TYPE = "Udvidet fordeling";
    public static final String PARAMETER_NAME_TYPE = "Parameternavn";

    // date format
    @Transient
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public LogEntry() {
        //for JPA
        this.timeStamp = new Date();
        this.friendlyTimeStamp = dateFormat.format(timeStamp);
        this.kle = "";
        this.user = "";
        this.operation = "";
        this.type = "";
        this.data = "";
        this.orgUnit = "";
        this.employment = "";
        this.municipality = null;

    }

    public LogEntry(String kle, String user, String operation, String type, String data, String orgUnit, String employment, Municipality municipality) {
        this.timeStamp = new Date();
        this.friendlyTimeStamp = dateFormat.format(timeStamp);

        this.kle = kle;
        this.user = user;
        this.operation = operation;
        this.type = type;
        this.data = data;
        this.orgUnit = orgUnit;
        this.employment = employment;
        this.municipality = municipality;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getFriendlyTimeStamp() {
        return dateFormat.format(timeStamp);
    }

    public void setFriendlyTimeStamp(String friendlyTimeStamp) {
        this.friendlyTimeStamp = friendlyTimeStamp;
    }

    public Municipality getMunicipality() {
        return municipality;
    }

    public void setMunicipality(Municipality municipality) {
        this.municipality = municipality;
    }

    public String getKle() {
        return kle;
    }

    public void setKle(String kle) {
        this.kle = kle;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getOrgUnit() {
        return orgUnit;
    }

    public void setOrgUnit(String orgUnit) {
        this.orgUnit = orgUnit;
    }

    public String getEmployment() {
        return employment;
    }

    public void setEmployment(String employment) {
        this.employment = employment;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
            "id=" + id +
            ", timeStamp=" + timeStamp +
            ", friendlyTimeStamp='" + friendlyTimeStamp + '\'' +
            ", kle='" + kle + '\'' +
            ", user='" + user + '\'' +
            ", operation='" + operation + '\'' +
            ", type='" + type + '\'' +
            ", data='" + data + '\'' +
            ", orgUnit='" + orgUnit + '\'' +
            ", employment='" + employment + '\'' +
            ", municipality=" + municipality +
            ", dateFormat=" + dateFormat +
            '}';
    }

    public String[] toStringArray() {
        return new String[]{Integer.toString(id), getFriendlyTimeStamp(), kle, user, operation, type, data, orgUnit, employment};
    }

    private String toJson() {
        return Json.createObjectBuilder().
                add("id", id).
                add("timeStamp", getFriendlyTimeStamp()).
                add("kle", kle).
                add("user", user).
                add("operation", operation).
                add("type", type).
                add("data", data).
                add("orgUnit", orgUnit).
                add("employment", employment).build().toString();
    }

}