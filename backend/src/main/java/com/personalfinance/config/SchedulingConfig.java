package com.personalfinance.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Планировщик вынесен из класса приложения, чтобы его можно было выключить —
 * в тестах генерация запланированных платежей вызывается явно, иначе фоновая
 * задача делала бы результаты недетерминированными.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
