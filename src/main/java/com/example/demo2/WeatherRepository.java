package com.example.demo2;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

interface WeatherRepository extends JpaRepository<WeatherData, Long> {

}
