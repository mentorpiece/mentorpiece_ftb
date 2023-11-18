package com.aerotravel.flightticketbooking.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "airportCode")
public class Airport implements Comparable<Airport>{
    @OneToMany(mappedBy = "departureAirport")
    @Builder.Default
    @JsonManagedReference("airport-flights")
    List<Flight> flights = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long airportId;
    @Column(unique = true)
    private String airportCode;
    @Size(max = 300)
    private String airportName;
    @Size(max = 300)
    private String city;
    @Size(max = 300)
    private String state;
    @Size(max = 300)
    private String country;

    public Airport(String airportCode, String airportName, String city, String state, String country) {
        this.airportCode = airportCode;
        this.airportName = airportName;
        this.city = city;
        this.state = state;
        this.country = country;
    }

    @Override
    public String toString() {
        return "Airport{" +
                "airportId=" + airportId +
                ", airportCode='" + airportCode + '\'' +
                ", airportName='" + airportName + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", flights=" + flights.stream()
                .filter(Objects::nonNull).map(Flight::getFlightNumber).toList() +
                '}';
    }

    public String describe() {
        return "Airport{" +
                "airportId=" + airportId +
                ", airportCode='" + airportCode + '\'' +
                ", airportName='" + airportName + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                '}';
    }

    @Override
    public int compareTo(Airport that) {
        if (null == that) {
            return -1;
        }

        if (null == that.airportName || null == this.airportName) {
            return Long.compare(this.airportId, that.airportId);
        }

        return this.airportName.compareTo(that.airportName);
    }
}
