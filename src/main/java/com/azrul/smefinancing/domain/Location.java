package com.azrul.smefinancing.domain;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.io.Serializable;
import java.util.Objects;

import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.domain.Reference;

@Entity
public class Location extends Reference implements Serializable {


    @WorkField(displayName = "District",sortable = true)
    @Column(name = "district", nullable = false)
    private String district;

    @WorkField(displayName = "State",sortable = true)
    @Column(name = "state", nullable = false)
    private String state;

    @WorkField(displayName = "Country",sortable = true)
    @Column(name = "country", nullable = false)
    private String country;

    // Default constructor
    public Location() {}

    // Constructor with fields
    public Location(String district, String state, String country) {
        this.district = district;
        this.state = state;
        this.country = country;
    }


    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

   

    // equals and hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(id, location.id) &&
               Objects.equals(district, location.district) &&
               Objects.equals(state, location.state) &&
               Objects.equals(country, location.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, district, state, country);
    }

    // toString method
    @Override
    public String toString() {
        return "Location{" +
               "id=" + id +
               ", district='" + district + '\'' +
               ", state='" + state + '\'' +
               ", country='" + country + '\'' +
               '}';
    }
}
