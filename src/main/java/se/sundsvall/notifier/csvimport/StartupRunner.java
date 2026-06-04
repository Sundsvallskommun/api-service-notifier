package se.sundsvall.notifier.csvimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;
import se.sundsvall.notifier.csvimport.scheduler.Scheduler;

/**
 * Runs an initial CSV import once the context is ready — in the background and non-fatally.
 *
 * <p>
 * Merge precondition (IMPROVEMENTS D1.9): in the consolidated deployable the import must never
 * block application boot or crash the JVM. A crisis-messaging API that refuses to start because an
 * SFTP server is unreachable is unacceptable, so this runs on the bounded async executor after
 * {@link ApplicationReadyEvent} and swallows failures — the hourly {@code @Dept44Scheduled} jobs
 * retry on their own cadence.
 */
@ExcludeFromJacocoGeneratedCoverageReport
@Component
class StartupRunner {

	private static final Logger LOG = LoggerFactory.getLogger(StartupRunner.class);

	private final Scheduler scheduler;

	StartupRunner(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Async
	@EventListener(ApplicationReadyEvent.class)
	void runInitialImport() {
		runSafely("[ORG]", scheduler::importOrganizationsJob);
		runSafely("[EMP]", scheduler::importEmployeesJob);
	}

	private void runSafely(String label, Runnable job) {
		try {
			job.run();
		} catch (Exception e) {
			LOG.error("{} startup import failed; scheduled jobs will retry", label, e);
		}
	}
}
