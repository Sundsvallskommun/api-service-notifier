package se.sundsvall.notifier;

import java.util.concurrent.Executor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Bounded executor backing every {@code @Async} call (message fan-out in the {@code messaging}
 * module and the background startup import in {@code csvimport}).
 *
 * <p>
 * Merge precondition (IMPROVEMENTS B1.2 / B1.3): the Spring default {@code SimpleAsyncTaskExecutor}
 * spawns an unbounded thread per call and would OOM the shared JVM under a broadcast; an
 * uncaught-exception handler makes async failures visible instead of silently swallowed.
 *
 * <p>
 * TODO (follow-up): bulkhead the CSV import onto its own small executor so a large import cannot
 * starve message fan-out, and size the pools against the Hikari connection pool (IMPROVEMENTS B2.8).
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {

	@Override
	public Executor getAsyncExecutor() {
		final var executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(4);
		executor.setMaxPoolSize(16);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("async-");
		executor.initialize();
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new SimpleAsyncUncaughtExceptionHandler();
	}
}
