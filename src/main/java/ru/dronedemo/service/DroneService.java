package ru.dronedemo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dronedemo.exception.ResourceNotFoundException;
import ru.dronedemo.model.DroneModel;
import ru.dronedemo.repository.DroneRepository;

@Service
@RequiredArgsConstructor
public class DroneService {

    private final DroneRepository repository;

    @Transactional(readOnly = true)
    public Page<DroneModel> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public DroneModel findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Дрон с ID " + id + " не найден"));
    }

    @Transactional
    public DroneModel create(DroneModel drone) {
        return repository.save(drone);
    }

    @Transactional
    public DroneModel update(Long id, DroneModel droneDto) {
        DroneModel existing = findById(id);

        if (droneDto.getName() != null) existing.setName(droneDto.getName());
        if (droneDto.getManufacturer() != null) existing.setManufacturer(droneDto.getManufacturer());
        if (droneDto.getType() != null) existing.setType(droneDto.getType());
        if (droneDto.getMaxTakeoffWeight() != null) existing.setMaxTakeoffWeight(droneDto.getMaxTakeoffWeight());
        if (droneDto.getMaxFlightTime() != null) existing.setMaxFlightTime(droneDto.getMaxFlightTime());
        if (droneDto.getReleaseYear() != null) existing.setReleaseYear(droneDto.getReleaseYear());
        if (droneDto.getDescription() != null) existing.setDescription(droneDto.getDescription());
        if (droneDto.getMaxRange() != null) existing.setMaxRange(droneDto.getMaxRange());
        if (droneDto.getPayloadCapacity() != null) existing.setPayloadCapacity(droneDto.getPayloadCapacity());
        if (droneDto.getSensorType() != null) existing.setSensorType(droneDto.getSensorType());
        if (droneDto.getIpRating() != null) existing.setIpRating(droneDto.getIpRating());

        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        DroneModel drone = findById(id);
        repository.delete(drone);
    }

    @Transactional(readOnly = true)
    public Page<DroneModel> filterDrones(String type, String manufacturer, Double minWeight, Integer minFlightTime, Pageable pageable) {
        Specification<DroneModel> spec = Specification.where(null);
        if (type != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("type"), type));
        if (manufacturer != null) spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("manufacturer")), "%" + manufacturer.toLowerCase() + "%"));
        if (minWeight != null) spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("maxTakeoffWeight"), minWeight));
        if (minFlightTime != null) spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("maxFlightTime"), minFlightTime));
        return repository.findAll(spec, pageable);
    }
}