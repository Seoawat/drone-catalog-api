package ru.dronedemo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.dronedemo.model.DroneModel;
import ru.dronedemo.repository.DroneRepository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final DroneRepository repository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Очищаем таблицу от кривых данных перед новой загрузкой
        if (repository.count() > 0) repository.deleteAll();

        System.out.println("Загружаем данные из drones.csv");
        InputStream is = getClass().getClassLoader().getResourceAsStream("drones.csv");
        if (is == null) {
            System.err.println("❌ Файл drones.csv не найден!");
            return;
        }

        int loaded = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            boolean skipHeader = true;

            while ((line = reader.readLine()) != null) {
                if (skipHeader) { skipHeader = false; continue; }
                if (line.isBlank()) continue;

                String[] cols = parseCsvLine(line);
                if (cols.length < 5) continue;

                DroneModel drone = DroneModel.builder()
                        .name(cols[0])
                        .description(cols[1])
                        .type(cols[2])
                        .maxTakeoffWeight(parseDouble(cols[3]))
                        .maxFlightTime(parseInt(cols[4]))
                        .manufacturer(cols[5]) // Теперь читаем реального производителя из CSV
                        .releaseYear(parseInt(cols[6]))
                        .maxRange(parseDouble(cols[7]))
                        .payloadCapacity(parseDouble(cols[8]))
                        .sensorType(cols[9])
                        .ipRating(cols[10])
                        .build();

                repository.save(drone);
                loaded++;
            }
        }
        System.out.println("✅ Загружено " + loaded + " записей.");
    }

    // Парсер, который учитывает кавычки и не ломается на запятых внутри текста
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"') inQuotes = !inQuotes;
            else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result.toArray(new String[0]);
    }

    private Double parseDouble(String v) { try { return v.isBlank() ? null : Double.parseDouble(v.replace(",", ".")); } catch(Exception e){ return null; } }
    private Integer parseInt(String v) { try { return v.isBlank() ? null : Integer.parseInt(v); } catch(Exception e){ return null; } }
}