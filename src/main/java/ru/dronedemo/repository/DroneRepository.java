package ru.dronedemo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.dronedemo.model.DroneModel;

@Repository
public interface DroneRepository extends JpaRepository<DroneModel, Long>, JpaSpecificationExecutor<DroneModel> {

    // Поиск по производителю
    Page<DroneModel> findByManufacturer(String manufacturer, Pageable pageable);

    // Поиск по типу
    Page<DroneModel> findByType(String type, Pageable pageable);

    // Поиск по названию (частичное совпадение, без учета регистра)
    Page<DroneModel> findByNameContainingIgnoreCase(String name, Pageable pageable);
}