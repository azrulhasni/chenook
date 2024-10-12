/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.smefinancing.domain;

import com.azrul.chenook.annotation.Matcher;
import com.azrul.chenook.annotation.NotBlankValue;
import com.azrul.chenook.annotation.NotNullValue;
import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.domain.converter.LocalDateTimeConverter;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.ValueConverter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author azrul
 */
@Entity
@Audited
@EntityListeners(AuditingEntityListener.class)
public class Applicant {

   
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    @WorkField(displayName = "User id number")
    private Long id;
    
    @NotBlankValue
    @WorkField(displayName = "Full name")
    private String fullName;
    
    @NotBlankValue
    @Matcher(regexp="((\\d{2}(?!0229))|([02468][048]|[13579][26])(?=0229))(0[1-9]|1[0-2])(0[1-9]|[12]\\d|(?<!02)30|(?<!02|4|6|9|11)31)\\-(\\d{2})\\-(\\d{4})",
            message="The NRIC number must of correct format (E.g. 781231-14-0123")
    @WorkField(displayName = "NRIC number")
    private String icNumber;
    
    @ValueConverter(LocalDateTimeConverter.class)
    @NotNullValue
    @WorkField(displayName = "Date of birth")
    private LocalDate dateOfBirth;
    
    
    @NotBlankValue
    @WorkField(displayName = "Phone number")
    private String phoneNumber;
    
    @NotBlankValue
    @Matcher(regexp="([!#-'*+/-9=?A-Z^-~-]+(\\.[!#-'*+/-9=?A-Z^-~-]+)*|\"([]!#-[^-~ \\t]|(\\\\[\\t -~]))+\")@([!#-'*+/-9=?A-Z^-~-]+(\\.[!#-'*+/-9=?A-Z^-~-]+)*|\\[[\\t -Z^-~]*])",message="The email address must be valid")
    @WorkField(displayName = "Email")
    private String email;

    private Boolean main;
    
    @WorkField(displayName = "Position")
    private ApplicantType type;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "applicant_error_mapping", 
      joinColumns = {@JoinColumn(name = "id", referencedColumnName = "id")})
    private Set<String> errors = new HashSet<>();
    
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
    
    @Transient
    @ManyToOne
    @JoinColumn(name = "fk_finApplication")
    private FinApplication finApplication;
    
//    @OneToMany(mappedBy = "applicant",orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    private Set<Attachment> documents = new HashSet<>();

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the icNumber
     */
    public String getIcNumber() {
        return icNumber;
    }

    /**
     * @param icNumber the icNumber to set
     */
    public void setIcNumber(String icNumber) {
        this.icNumber = icNumber;
    }

//    /**
//     * @return the position
//     */
//    public String getPosition() {
//        return position;
//    }
//
//    /**
//     * @param position the position to set
//     */
//    public void setPosition(String position) {
//        this.position = position;
//    }

    /**
     * @return the phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @param phoneNumber the phoneNumber to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    

    /**
     * @return the type
     */
    public ApplicantType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(ApplicantType type) {
        this.type = type;
    }

    /**
     * @return the business
     */
    public FinApplication getFinApplication() {
        return finApplication;
    }

    /**
     * @param business the business to set
     */
    public void setFinApplication(FinApplication finApplication) {
        this.finApplication = finApplication;
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
     * @return the primary
     */
    public Boolean getMain() {
        return main;
    }

    /**
     * @param primary the primary to set
     */
    public void setMain(Boolean main) {
        this.main = main;
    }



    /**
     * @return the dateOfBirth
     */
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * @param dateOfBirth the dateOfBirth to set
     */
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.id);
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
        final Applicant other = (Applicant) obj;
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

  

}
