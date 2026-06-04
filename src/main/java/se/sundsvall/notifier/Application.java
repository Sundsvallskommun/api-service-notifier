package se.sundsvall.notifier;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableAsync;
import se.sundsvall.dept44.ServiceApplication;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

import static org.springframework.boot.SpringApplication.run;

/**
 * Merged crisis-communication backend — a Spring Modulith app with three business modules
 * ({@code messaging}, {@code users}, {@code csvimport}) plus a shared {@code security} module.
 *
 * <ul>
 * <li>{@code @EnableAsync} — message fan-out + background startup import (bounded by {@link AsyncConfig})</li>
 * <li>{@code @EnableFeignClients} — outbound sms-sender / teams-sender clients (messaging)</li>
 * <li>scheduling is enabled by {@code csvimport}'s {@code Scheduler} (@EnableScheduling + @Dept44Scheduled)</li>
 * <li>{@code sharedModules = "security"} — every module may use the shared JWT/security infra</li>
 * </ul>
 */
@EnableAsync
@EnableFeignClients
@Modulithic(sharedModules = "security")
@ServiceApplication
@ExcludeFromJacocoGeneratedCoverageReport
public class Application {
	public static void main(final String... args) {
		run(Application.class, args);
	}
}
