package com.example.academy.support;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Tag("integration")
public abstract class IntegrationSupportTest {

}
