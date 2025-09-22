package com.aerotravel.flightticketbooking.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "aircraftId")
public class Aircraft implements Comparable<Aircraft>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long aircraftId;
    @Size(max = 300)
    private String manufacturer;
    @Size(max = 300)
    private String model;
    @Max(value = 1000, message = "* Number of seats cannot be too big.")
    @Min(value = 1, message = "* Number of seats cannot be too small.")
    private Integer numberOfSeats;
    @OneToMany(mappedBy = "aircraft")
    @Builder.Default
    @JsonManagedReference("aircraft-flights")
    private List<Flight> flights = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    public Aircraft(String manufacturer, String model, Integer numberOfSeats) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.numberOfSeats = numberOfSeats;
        this.flights = new ArrayList<>();
    }

    public Aircraft(long id, String manufacturer, String model, Integer numberOfSeats) {
        this.aircraftId = id;
        this.manufacturer = manufacturer;
        this.model = model;
        this.numberOfSeats = numberOfSeats;
        this.flights = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Aircraft{" +
                "aircraftId=" + aircraftId +
                ", manufacturer='" + manufacturer + '\'' +
                ", model='" + model + '\'' +
                ", numberOfSeats=" + numberOfSeats +
                ", flights=" + flights.stream()
                .filter(Objects::nonNull).map(Flight::getFlightNumber).toList() +
                '}';
    }

    public String describe() {
        return "Aircraft{" +
                "aircraftId=" + aircraftId +
                ", manufacturer='" + manufacturer + '\'' +
                ", model='" + model + '\'' +
                ", numberOfSeats=" + numberOfSeats +
                '}';
    }

    @Override
    public int compareTo(Aircraft that) {
        if (null == that) {
            return -1;
        }

        if (null == that.model || null == this.model) {
            return Long.compare(this.aircraftId, that.aircraftId);
        }

        return this.model.compareTo(that.model);
    }

}
