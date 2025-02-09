package com.weatherapp.myweatherapp.service;

import com.weatherapp.myweatherapp.model.CityInfo;
import com.weatherapp.myweatherapp.repository.VisualcrossingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.Duration;
import java.util.Map;

@Service
public class WeatherService {
    private final VisualcrossingRepository weatherRepo;

    @Autowired
    public WeatherService(VisualcrossingRepository weatherRepo) {
        this.weatherRepo = weatherRepo;
    }

    /**
     * Validates and retrieves weather data for a given city
     * @param city Name of the city to validate
     * @return CityInfo object containing weather data
     * @throws IllegalArgumentException if city data is unavailable
     */
    private CityInfo validateAndGetCityData(String city) {
        CityInfo info = weatherRepo.getByCity(city);
        if (info == null) {
            throw new IllegalArgumentException("Weather data not available for: " + city);
        }
        return info;
    }

    public CityInfo forecastByCity(String city) {
        return validateAndGetCityData(city);
    }

    /**
     * Compares the daylight hours between two cities and determines which has the longer day
     * @param city1 Name of first city to compare
     * @param city2 Name of second city to compare
     * @return Map containing the city with longer daylight and detailed comparison
     * @throws IllegalArgumentException if city data is unavailable
     */
    public Map<String, String> compareDaylightHours(String city1, String city2) {
        CityInfo info1 = validateAndGetCityData(city1);
        CityInfo info2 = validateAndGetCityData(city2);

        Duration daylight1 = calculateDaylight(info1);
        Duration daylight2 = calculateDaylight(info2);
        
        return compareDurations(daylight1, daylight2, city1, city2);
    }

    /**
     * Calculates total daylight duration for a given city
     * @param info CityInfo object containing sunrise and sunset data
     * @return Duration object representing total daylight hours
     */
    private Duration calculateDaylight(CityInfo info) {
        LocalTime sunrise = LocalTime.parse(info.getCurrentConditions().getSunrise());
        LocalTime sunset = LocalTime.parse(info.getCurrentConditions().getSunset());
        return Duration.between(sunrise, sunset);
    }

    /**
     * Compares two durations and creates a formatted result
     * @return Map containing comparison results and message
     */
    private Map<String, String> compareDurations(Duration d1, Duration d2, String city1, String city2) {
        String longerDay;
        Duration difference;
        
        if (d1.compareTo(d2) > 0) {
            longerDay = city1;
            difference = d1.minus(d2);
        } else {
            longerDay = city2;
            difference = d2.minus(d1);
        }

        return Map.of(
            "city", longerDay,
            "details", String.format("%s has longer daylight by %d hours and %d minutes",
                longerDay, difference.toHours(), difference.toMinutesPart())
        );
    }

    /**
     * Checks the current rain conditions in two cities
     * @param city1 Name of first city to check
     * @param city2 Name of second city to check
     * @return Map containing rain status and detailed description
     * @throws IllegalArgumentException if city data is unavailable
     */
    public Map<String, String> checkRainConditions(String city1, String city2) {
        CityInfo info1 = validateAndGetCityData(city1);
        CityInfo info2 = validateAndGetCityData(city2);

        boolean isRaining1 = isRaining(info1);
        boolean isRaining2 = isRaining(info2);

        return createRainStatus(isRaining1, isRaining2, city1, city2);
    }

    /**
     * Checks if current conditions are rainy
     */
    private boolean isRaining(CityInfo info) {
        return info.getCurrentConditions().getConditions()
                  .toLowerCase().contains("rain");
    }

    /**
     * Creates a message based on rain conditions in both cities
     */
    private Map<String, String> createRainStatus(boolean rain1, boolean rain2, 
                                               String city1, String city2) {
        if (rain1 && rain2) {
            return Map.of("status", "both",
                         "details", "It is currently raining in both " + city1 + " and " + city2);
        } else if (rain1) {
            return Map.of("status", city1,
                         "details", "It is currently raining in " + city1);
        } else if (rain2) {
            return Map.of("status", city2,
                         "details", "It is currently raining in " + city2);
        }
        return Map.of("status", "none",
                     "details", "It is not currently raining in either city");
    }
}

