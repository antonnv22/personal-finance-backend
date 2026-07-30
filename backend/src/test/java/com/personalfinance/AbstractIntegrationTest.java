package com.personalfinance;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Базовый класс интеграционных тестов.
 *
 * <p>Vault отключается именно здесь, а не в application-test.yml: свойства из
 * {@code @SpringBootTest} попадают в окружение до разбора config data, тогда как
 * профильный файл подключается уже после обработки {@code spring.config.import}.
 * Сам импорт в application.yml помечен как {@code optional:}, поэтому при
 * выключенном Vault он просто пропускается. Благодаря этому тесты не требуют ни
 * живого Vault, ни VAULT_TOKEN.
 */
@SpringBootTest(properties = "spring.cloud.vault.enabled=false")
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
}
