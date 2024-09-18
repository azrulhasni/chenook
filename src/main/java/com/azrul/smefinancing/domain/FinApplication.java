/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.domain;

import com.azrul.chenook.annotation.DateTimeFormat;
import com.azrul.chenook.annotation.Matcher;
import com.azrul.chenook.annotation.NotBlankValue;
import com.azrul.chenook.annotation.NotNullValue;
import com.azrul.chenook.annotation.Range;
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

/**
 *
 * @author azrul
 */
@Entity
@DiscriminatorValue("FIN_APP")
@Audited
@EntityListeners(AuditingEntityListener.class)
public class FinApplication extends WorkItem {

//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    @Column(name = "id")
//    private Long id;
    @NotBlankValue
    @WorkField(displayName = "Name")
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

    @NotBlankValue
    @WorkField(displayName = "State")
    private String state;

    @NotBlankValue
    @WorkField(displayName = "Main business activity")
    private String mainBusinessActivity;

    @NotNullValue
    @Range(min = 1000, max=50000, message = "Financing requested should be more than MYR 1000 and less than MYR50000")
    @WorkField(displayName = "Financing requested", prefix = "MYR")
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

    @NotNullValue
    @WorkField(displayName = "Application date")
    @DateTimeFormat(format = "${finapp.datetime.format}")
    private LocalDateTime applicationDate;

    @NotBlankValue
    @WorkField(displayName = "Reason for financing")
    @Size(max = 255, message
            = "Reason for financing must be of at most 255 characters")
    private String reasonForFinancing;

//    @Transient 
//    protected NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(); 
    @OneToMany(mappedBy = "finApplication", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Applicant> applicants = new HashSet<>();

    @Audited(withModifiedFlag = true)
    private Integer version;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private LocalDateTime creationDate;

    @LastModifiedBy
    private String lastModifiedBy;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "finapplication_error_mapping",
            joinColumns = {
                @JoinColumn(name = "id", referencedColumnName = "id")})
    private Set<String> errors = new HashSet<>();

//    /**
//     * @return the id
//     */
//    public Long getId() {
//        return id;
//    }
//
//    /**
//     * @param id the id to set
//     */
//    public void setId(Long id) {
//        this.id = id;
//    }
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

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

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
    @WorkField(displayName = "Application ID (AA Number)")
    public Long getId() {
        return super.getId();
    }

    @Override
    @WorkField(displayName = "Worklist")
    public String getWorklist() {
        return super.getWorklist();
    }

    @Override
    @WorkField(displayName = "Worklist Last Update")
    public LocalDateTime getWorklistUpdateTime() {
        return super.getWorklistUpdateTime();
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
            return "SME Financing (" + this.getFinancingRequested().getCurrency().getCurrencyCode()+ this.getFinancingRequested().getNumber().toString()+")";
        } else {
            return "SME Financing";
        }
    }
}
