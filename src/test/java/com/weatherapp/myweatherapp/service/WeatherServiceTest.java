package com.weatherapp.myweatherapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.weatherapp.myweatherapp.model.CityInfo;
import com.weatherapp.myweatherapp.repository.VisualcrossingRepository;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private VisualcrossingRepository weatherRepo;

    @InjectMocks
    private WeatherService weatherService;

    /** 
     * Helper method to create CityInfo objects with test weather data
     */
    private CityInfo createCityInfo(String sunrise, String sunset, String conditions) {
        CityInfo cityInfo = new CityInfo();
        CityInfo.CurrentConditions currentConditions = new CityInfo.CurrentConditions();
        currentConditions.setSunrise(sunrise);
        currentConditions.setSunset(sunset);
        currentConditions.setConditions(conditions);
        cityInfo.setCurrentConditions(currentConditions);
        return cityInfo;
    }

    /**
     * Tests daylight comparison when the first city has longer daylight hours.
     * Verifies both the city identification and the time difference calculation.
     * 
     * Expected: London (16 hours) vs Paris (14 hours)
     * Result should identify London as having longer daylight by 2 hours
     */
    @Test
void compareDaylightHours_FirstCityLonger() {
    // Arrange: Mock data where London (city1) has longer daylight hours than Paris (city2)
    CityInfo london = createCityInfo("05:00:00", "21:00:00", "Clear");
    CityInfo paris = createCityInfo("06:00:00", "20:00:00", "Clear");
    
    when(weatherRepo.getByCity("London")).thenReturn(london);
    when(weatherRepo.getByCity("Paris")).thenReturn(paris);

    // Act: Call the method to compare daylight hours
    Map<String, String> result = weatherService.compareDaylightHours("London", "Paris");

    // Assert: Verify that code returns London as having longer daylight hours
    assertEquals("London", result.get("city"));
    assertTrue(result.get("details").contains("London has longer daylight by 2 hours"));
}


/**
     * Tests daylight comparison when the second city has longer daylight hours.
     * Verifies accurate comparison when the longer day is in the second city.
     * 
     * Expected: London (14 hours) vs Castries (16 hours)
     * Result should identify Castries as having longer daylight by 2 hours
     */
    @Test
    void compareDaylightHours_SecondCityLonger() {
      // Arrange: Mock data where Castries (city2) has longer daylight hours than London (city1)
      CityInfo london = createCityInfo("06:00:00", "20:00:00", "Clear");
      CityInfo castries = createCityInfo("05:00:00", "21:00:00", "Clear");

      when(weatherRepo.getByCity("London")).thenReturn(london);
      when(weatherRepo.getByCity("Castries")).thenReturn(castries);

      // Act
      Map<String, String> result = weatherService.compareDaylightHours("London", "Castries");

      // Assert
      assertEquals("Castries", result.get("city"));
      assertTrue(result.get("details").contains("Castries has longer daylight by 2 hours"));
    }

    /**
     * Tests rain condition detection when both cities are experiencing rain.
     * Verifies the system can handle different types of rain (Rain vs Heavy Rain).
     * 
     * Expected: Both cities should be identified as having rain
     * Status should be "both" with appropriate description
     */
    @Test
    void checkRainConditions_BothCitiesRaining() {
        // Arrange: Mock data where both London and Amsterdam are experiencing rain
        CityInfo london = createCityInfo("06:00:00", "20:00:00", "Rain");
        CityInfo amsterdam = createCityInfo("06:00:00", "20:00:00", "Heavy Rain");
        
        when(weatherRepo.getByCity("London")).thenReturn(london);
        when(weatherRepo.getByCity("Amsterdam")).thenReturn(amsterdam);

        // Act
        Map<String, String> result = weatherService.checkRainConditions("London", "Amsterdam");

        // Assert
        assertEquals("both", result.get("status"));
        assertTrue(result.get("details").contains("both London and Amsterdam"));
    }

    /**
     * Tests rain condition detection when only the first city has rain.
     * Verifies correct identification when rain is present in only one location.
     * 
     * Expected: Only London should be identified as having rain
     * Status should be "London" with appropriate description
     */
    @Test
    void checkRainConditions_FirstCityRaining() {
        // Arrange: Mock data where it is raining in London(city 1) but not Amsterdam (city2)
        CityInfo london = createCityInfo("06:00:00", "20:00:00", "Rain");
        CityInfo amsterdam = createCityInfo("06:00:00", "20:00:00", "Clear");
        
        when(weatherRepo.getByCity("London")).thenReturn(london);
        when(weatherRepo.getByCity("Amsterdam")).thenReturn(amsterdam);

        // Act
        Map<String, String> result = weatherService.checkRainConditions("London", "Amsterdam");

        // Assert
        assertEquals("London", result.get("status"));
        assertTrue(result.get("details").contains("raining in London"));
    }

    /**
     * Tests rain condition detection when only the second city has rain.
     * Verifies the system correctly identifies rain in the second city only.
     * 
     * Expected: Only Amsterdam should be identified as having rain
     * Status should be "Amsterdam" with appropriate description
     */
    @Test
    void checkRainConditions_SecondCityRaining() {
        // Arrange: Mock data where it is raining in Amsterdam (city 2) but not London (city1)
        CityInfo london = createCityInfo("06:00:00", "20:00:00", "Clear");
        CityInfo amsterdam = createCityInfo("06:00:00", "20:00:00", "Rain");
        
        when(weatherRepo.getByCity("London")).thenReturn(london);
        when(weatherRepo.getByCity("Amsterdam")).thenReturn(amsterdam);

        // Act
        Map<String, String> result = weatherService.checkRainConditions("London", "Amsterdam");

        // Assert
        assertEquals("Amsterdam", result.get("status"));
        assertTrue(result.get("details").contains("raining in Amsterdam"));
    }

    /**
     * Tests rain condition detection when neither city has rain.
     * Verifies system handles non-rain conditions (Clear, Cloudy) correctly.
     * 
     * Expected: Neither city should be identified as having rain
     * Status should be "none" with appropriate description
     */
    @Test
    void checkRainConditions_NoCityRaining() {
        // Arrange: Mock data where it is not raining in either city
        CityInfo london = createCityInfo("06:00:00", "20:00:00", "Clear");
        CityInfo amsterdam = createCityInfo("06:00:00", "20:00:00", "Cloudy");
        
        when(weatherRepo.getByCity("London")).thenReturn(london);
        when(weatherRepo.getByCity("Amsterdam")).thenReturn(amsterdam);

        // Act
        Map<String, String> result = weatherService.checkRainConditions("London", "Amsterdam");

        // Assert
        assertEquals("none", result.get("status"));
        assertTrue(result.get("details").contains("not currently raining"));
    }
}