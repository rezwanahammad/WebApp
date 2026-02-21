package com.example.webapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class WebappApplicationTests {

	@Container
	@ServiceConnection
	@SuppressWarnings("unused") // Used by Testcontainers framework
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

	@Test
	void contextLoads() {
	}

}

