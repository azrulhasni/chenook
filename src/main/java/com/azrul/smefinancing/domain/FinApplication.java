/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.domain;

import com.azrul.chenook.annotation.DateTimeFormat;
import com.azrul.chenook.annotation.Matcher;
import com.azrul.chenook.annotation.NotBlankValue;
import com.azrul.chenook.annotation.NotEmpty;
import com.azrul.chenook.annotation.NotNullValue;
import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.domain.WorkItem;
import io.hypersistence.utils.hibernate.type.money.MonetaryAmountType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.money.MonetaryAmount;
import org.hibernate.annotations.CompositeType;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.azrul.chenook.annotation.NumberRange;
import com.azrul.chenook.annotation.SingleValue;
import com.azrul.chenook.domain.Status;
import com.azrul.chenook.domain.converter.LocalDateTimeConverter;
import com.azrul.chenook.domain.converter.MoneyConverter;
import java.text.NumberFormat;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.ValueConverter;

/**
 *
 * @author azrul
 */

@Entity
@DiscriminatorValue("FIN_APP")
@Audited
@EntityListeners(AuditingEntityListener.class)
public class FinApplication extends WorkItem {


    @NotBlankValue
    @WorkField(displayName = "Name", sortable=true)
    private String name;

    @NotBlankValue
    @WorkField(displayName = "SSM Registration Number")
    private String ssmRegistrationNumber;

    @NotBlankValue
    @WorkField(displayName = "Address")
    private String address;

    @NotBlankValue
    @Matcher(regexp = "[0-9]{5}", message = "Postal code must follow format (e.g. 12345)")
    @WorkField(displayName = "Postal Code")
    private String postalCode;

    @NotEmpty(message = "At least one location must be selected")
    //@SingleValue
    @WorkField(displayName = "Location")
    @ManyToMany( fetch = FetchType.EAGER)
	@JoinTable(name="finapp_locations", joinColumns=@JoinColumn(name="finapps_id"), inverseJoinColumns=@JoinColumn(name="location_id"))
    private Set<Location> location = new HashSet<>();

    @NotBlankValue
    @WorkField(displayName = "Main business activity")
    private String mainBusinessActivity;

    @ValueConverter(MoneyConverter.class)
    @NotNullValue
    @NumberRange(min = 1000, max=50000, message = "Financing requested should be more than MYR 1000 and less than MYR50000")
    @WorkField(displayName = "Financing requested", prefix = "MYR", sortable = true)
    @AttributeOverride(
            name = "amount",
            column = @Column(name = "financing_amount")
            
    )
    @AttributeOverride(
            name = "currency",
            column = @Column(name = "financing_currency")
    )
    @CompositeType(MonetaryAmountType.class)
    private MonetaryAmount financingRequested;

    @ValueConverter(LocalDateTimeConverter.class)
    @NotNullValue
    @WorkField(displayName = "Application date", sortable=true)
    @DateTimeFormat(format = "${finapp.datetime.format}")
    private LocalDateTime applicationDate;

    @NotBlankValue
    @WorkField(displayName = "Reason for financing")
    @Size(max = 255, message
            = "Reason for financing must be of at most 255 characters")
    private String reasonForFinancing;
    
    @WorkField(displayName = "Bureau Score")
    private Integer bureauScore;
    
    @WorkField(displayName = "Bureau Result")
    private String bureauResult;
    
    @WorkField(displayName = "Site Visit Report")
    private String siteVisitReport;


    @OneToMany(mappedBy = "finApplication", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Applicant> applicants = new HashSet<>();

    @Transient
    @Audited(withModifiedFlag = true)
    private Integer version;

    @Transient
    @CreatedBy
    private String createdBy;

    @Transient
    @CreatedDate
    private LocalDateTime creationDate;

    @Transient
    @LastModifiedBy
    private String lastModifiedBy;

    @Transient
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
    
    

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "finapplication_error_mapping",
            joinColumns = {
                @JoinColumn(name = "id", referencedColumnName = "id")})
    private Set<String> errors = new HashSet<>();


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the ssmRegustrationNumber
     */
    public String getSsmRegistrationNumber() {
        return ssmRegistrationNumber;
    }

    /**
     * @param ssmRegustrationNumber the ssmRegustrationNumber to set
     */
    public void setSsmRegistrationNumber(String ssmRegistrationNumber) {
        this.ssmRegistrationNumber = ssmRegistrationNumber;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /*public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }*/
   
    /**
     * @return the mainBusinessActivity
     */
    public String getMainBusinessActivity() {
        return mainBusinessActivity;
    }

    /**
     * @param mainBusinessActivity the mainBusinessActivity to set
     */
    public void setMainBusinessActivity(String mainBusinessActivity) {
        this.mainBusinessActivity = mainBusinessActivity;
    }

    /**
     * @return the contactPersons
     */
    public Set<Applicant> getApplicants() {
        return applicants;
    }

    /**
     * @param contactPersons the contactPersons to set
     */
    public void setApplicants(Set<Applicant> applicants) {
        this.applicants = applicants;
    }

    /**
     * @return the version
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    @WorkField(displayName = "Application ID (AA Number)",  sortable = true)
    public Long getId() {
        return super.getId();
    }

    @Override
    @WorkField(displayName = "Worklist")
    public String getWorklist() {
        return super.getWorklist();
    }

    @Override
    //@GenericField
    @WorkField(displayName = "Worklist Last Update")
    public LocalDateTime getWorklistUpdateTime() {
        return super.getWorklistUpdateTime();
    }
    
    @Override
    @WorkField(displayName = "Status", sortable=true)
    public Status getStatus() {
        return super.getStatus();
    }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the creationDate
     */
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the lastModifiedBy
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * @param lastModifiedBy the lastModifiedBy to set
     */
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /**
     * @return the lastModifiedDate
     */
    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * @param lastModifiedDate the lastModifiedDate to set
     */
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * @return the applicationDate
     */
    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    /**
     * @param applicationDate the applicationDate to set
     */
    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }

    /**
     * @return the financingRequested
     */
    public MonetaryAmount getFinancingRequested() {
        return financingRequested;
    }

    /**
     * @param financingRequested the financingRequested to set
     */
    public void setFinancingRequested(MonetaryAmount financingRequested) {
        this.financingRequested = financingRequested;
    }

    /**
     * @return the postalCode
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * @param postalCode the postalCode to set
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * @return the reasonForFinancing
     */
    public String getReasonForFinancing() {
        return reasonForFinancing;
    }

    /**
     * @param reasonForFinancing the reasonForFinancing to set
     */
    public void setReasonForFinancing(String reasonForFinancing) {
        this.reasonForFinancing = reasonForFinancing;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FinApplication other = (FinApplication) obj;
        return Objects.equals(this.id, other.id);
    }

    /**
     * @return the errors
     */
    public Set<String> getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(Set<String> errors) {
        this.errors = errors;
    }

    @Override
    public String getTitle() {
        
        if (this.getFinancingRequested() != null) {
            return "SME Financing ("
                    + this.getFinancingRequested().getCurrency().getCurrencyCode()
                    + " "
                    + NumberFormat.getInstance().format(this.getFinancingRequested().getNumber())
                    + ")";
        } else {
            return "SME Financing";
        }
    }

    /**
     * @return the bureauScore
     */
    public Integer getBureauScore() {
        return bureauScore;
    }

    /**
     * @param bureauScore the bureauScore to set
     */
    public void setBureauScore(Integer bureauScore) {
        this.bureauScore = bureauScore;
    }

    /**
     * @return the bureauResult
     */
    public String getBureauResult() {
        return bureauResult;
    }

    /**
     * @param bureauResult the bureauResult to set
     */
    public void setBureauResult(String bureauResult) {
        this.bureauResult = bureauResult;
    }

    /**
     * @return the siteVisitReport
     */
    public String getSiteVisitReport() {
        return siteVisitReport;
    }

    /**
     * @param siteVisitReport the siteVisitReport to set
     */
    public void setSiteVisitReport(String siteVisitReport) {
        this.siteVisitReport = siteVisitReport;
    }

    public void setLocation(Set<Location> location) {
        this.location = location;
    }

    public Set<Location> getLocation() {
        return location;
    }
}
