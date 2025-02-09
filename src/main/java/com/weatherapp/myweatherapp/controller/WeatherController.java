package com.weatherapp.myweatherapp.controller;

import com.weatherapp.myweatherapp.model.CityInfo;
import com.weatherapp.myweatherapp.service.WeatherService;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WeatherController {

  @Autowired
  WeatherService weatherService;

  @GetMapping("/forecast/{city}")
  public ResponseEntity<CityInfo> forecastByCity(@PathVariable("city") String city) {

    CityInfo ci = weatherService.forecastByCity(city);

    return ResponseEntity.ok(ci);
  }

  /**
     * Compares daylight hours between two cities and determines which has longer daylight.
     * @param city1 First city to compare
     * @param city2 Second city to compare
     * @return ResponseEntity containing comparison results or error message
     */
    @GetMapping("/daylight/{city1}/{city2}")
    public ResponseEntity<?> compareDaylightHours(
            @PathVariable String city1,
            @PathVariable String city2) {
        try {
            return ResponseEntity.ok(weatherService.compareDaylightHours(city1, city2));
        } catch (IllegalArgumentException e) {
            // One or both cities not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Other errors (e.g., parsing errors, calculation errors)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to compare daylight hours: " + e.getMessage()));
        }
    }

    /**
     * Checks which cities are experiencing rain
     * @param city1 First city to check
     * @param city2 Second city to check
     * @return ResponseEntity containing rain status or error message
     */
    @GetMapping("/rain/{city1}/{city2}")
    public ResponseEntity<?> checkRain(
            @PathVariable String city1,
        @PathVariable String city2) {
      try {
        return ResponseEntity.ok(weatherService.checkRainConditions(city1, city2));
      } catch (IllegalArgumentException e) {
        // One or both cities not found
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", e.getMessage()));
      } catch (Exception e) {
        // Other errors (e.g., API failures)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Failed to check rain conditions: " + e.getMessage()));
      }
    }
    
    /**
   * Health check endpoint to verify API availability
   * @return A simple confirmation message
   */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok("Weather API is functioning.");
}

}

