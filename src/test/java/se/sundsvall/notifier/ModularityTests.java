package se.sundsvall.notifier;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Enforces the modular-monolith boundaries at build time. Fails if any module reaches into
 * another module's internals or if a dependency cycle appears between modules — the guarantee that
 * keeps this consolidation a modular monolith rather than a single-process big ball of mud.
 *
 * <p>
 * Expected modules: {@code messaging}, {@code users}, {@code csvimport}, and the shared
 * {@code security} module (declared via {@code @Modulithic(sharedModules = "security")}).
 */
class ModularityTests {

	private final ApplicationModules modules = ApplicationModules.of(Application.class);

	@Test
	void verifiesModuleBoundaries() {
		modules.verify();
	}
}
