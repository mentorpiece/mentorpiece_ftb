package com.aerotravel.flightticketbooking.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "flightNumber")
public class Flight implements Comparable<Flight>{
    @ManyToOne
    @JsonBackReference("aircraft-flights")
    Aircraft aircraft;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;
    @OneToMany(mappedBy = "flight")
    @Builder.Default
    @JsonManagedReference("flight-passengers")
    List<Passenger> passengers = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long flightId;
    @Size(max = 30, message = "Not longer than 30 characters please!")
    @NotBlank
    private String flightNumber;
    @ManyToOne
    @JsonBackReference("airport-flights")
    private Airport departureAirport;
    @ManyToOne
    @JsonBackReference
    private Airport destinationAirport;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent
    private LocalDate departureDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent
    private LocalDate arrivalDate;
    @NotBlank
    private String departureTime;
    @NotBlank
    private String arrivalTime;
    private String gate;
    private String status;
    @PositiveOrZero(message = "Shall be positive!")
    private double flightCharge;

    public Flight(String flightNumber, Airport departureAirport, Airport destinationAirport,
                  double flightCharge, LocalDate departureDate, LocalDate arrivalDate) {

        this.flightNumber = flightNumber;
        this.departureAirport = departureAirport;
        this.destinationAirport = destinationAirport;
        this.flightCharge = flightCharge;
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "flightId=" + flightId +
                ", flightNumber='" + flightNumber + '\'' +
                ", departureAirport=" + (departureAirport == null ? null : departureAirport.getAirportCode()) +
                ", destinationAirport=" + (destinationAirport == null ? null : destinationAirport.getAirportCode()) +
                ", departureDate=" + departureDate +
                ", arrivalDate=" + arrivalDate +
                ", departureTime='" + departureTime + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", gate='" + gate + '\'' +
                ", status='" + status + '\'' +
                ", flightCharge=" + flightCharge +
                ", aircraft=" + (aircraft == null ? null : aircraft.getModel()) +
                ", passengers=" + passengers.stream().map(Passenger::getLastName).toList() +
                '}';
    }

    public String describe() {
        return "Flight{" +
                "flightId=" + flightId +
                ", flightNumber='" + flightNumber + '\'' +
                ", departureAirport=" + (departureAirport == null ? null : departureAirport.getAirportCode()) +
                ", destinationAirport=" + (destinationAirport == null ? null : destinationAirport.getAirportCode()) +
                ", departureDate=" + departureDate +
                ", arrivalDate=" + arrivalDate +
                ", departureTime='" + departureTime + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", gate='" + gate + '\'' +
                ", status='" + status + '\'' +
                ", flightCharge=" + flightCharge +
                ", aircraft=" + (aircraft == null ? null : aircraft.getModel()) +
                '}';
    }

    @Override
    public int compareTo(Flight that) {
        if (null == that) {
            return -1;
        }

        if (null == that.flightNumber || null == this.flightNumber) {
            return Long.compare(this.flightId, that.flightId);
        }

        return this.flightNumber.compareTo(that.flightNumber);
    }
}
