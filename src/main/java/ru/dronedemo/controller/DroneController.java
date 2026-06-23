package ru.dronedemo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dronedemo.model.DroneModel;
import ru.dronedemo.service.DroneService;

@RestController
@RequestMapping("/api/drones")
@RequiredArgsConstructor
public class DroneController {

    private final DroneService service;

    // GET /api/drones?page=0&size=10&sort=name
    @GetMapping
    public ResponseEntity<Page<DroneModel>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir) {

        Sort sorting = Sort.by(Sort.Direction.fromString(dir), sort);
        return ResponseEntity.ok(service.findAll(PageRequest.of(page, size, sorting)));
    }

    // ⬇️ ПЕРЕНЕСЛИ СЮДА — ДО /{id}, чтобы избежать конфликта маршрутов
    @GetMapping("/filter")
    public ResponseEntity<Page<DroneModel>> filter(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) Double minWeight,
            @RequestParam(required = false) Integer minFlightTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(service.filterDrones(type, manufacturer, minWeight, minFlightTime, PageRequest.of(page, size)));
    }

    // GET /api/drones/{id}
    @GetMapping("/{id}")
    public ResponseEntity<DroneModel> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    // POST /api/drones
    @PostMapping
    public ResponseEntity<DroneModel> create(@RequestBody @Valid DroneModel drone) {
        return ResponseEntity.status(201).body(service.create(drone));
    }

    // PUT /api/drones/{id}
    @PutMapping("/{id}")
    public ResponseEntity<DroneModel> update(
            @PathVariable Long id,
            @RequestBody @Valid DroneModel droneDto) {
        return ResponseEntity.ok(service.update(id, droneDto));
    }

    // DELETE /api/drones/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}