package ru.dronedemo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сущность дрона. Маппится на таблицу 'drones' в БД.
 */
@Entity
@Table(name = "drones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DroneModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название обязательно")
    private String name;

    private String manufacturer;

    // Тип: multirotor, fixed-wing, hybrid, helicopter
    @NotBlank(message = "Тип обязателен")
    private String type;

    private Double maxTakeoffWeight; // кг
    private Integer maxFlightTime;   // минуты
    private Integer releaseYear;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Дополнительные ТТХ
    private Double maxRange;          // км
    private Double payloadCapacity;   // кг
    private String sensorType;        // rgb, thermal и т.д.
    private String ipRating;          // IP54
}