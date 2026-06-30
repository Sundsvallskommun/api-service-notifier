package se.sundsvall.notifier.users.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import se.sundsvall.notifier.users.integration.db.UserRepository;
import se.sundsvall.notifier.users.integration.db.model.UserEntity;
import se.sundsvall.notifier.users.integration.db.model.enums.Role;
import se.sundsvall.notifier.users.integration.db.model.enums.Status;

@Component
@Profile("!(junit | it)")
public class DataInitializer implements CommandLineRunner {
	@Value("${user.credentials.email}")
	String email;
	@Value("${user.credentials.password}")
	String password;
	@Value("${user.credentials.phoneNumber}")
	String phoneNumber;
	@Value("${user.credentials.municipalityId}")
	String municipalityId;

	UserRepository userRepository;
	PasswordEncoder passwordEncoder;

	public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		// Bootstrap/repair the configured admin on every boot: create it if absent, otherwise force the
		// ADMIN role. This is a deliberate standing-privilege path — the account named by
		// user.credentials.email is guaranteed admin after each restart.
		var existing = userRepository.findByEmail(email);
		if (existing.isEmpty()) {
			userRepository.save(UserEntity.create()
				.withEmail(email)
				.withMunicipalityId(municipalityId)
				.withPhoneNumber(phoneNumber)
				.withStatus(Status.ACTIVE)
				.withRole(Role.ADMIN)
				.withPassword(passwordEncoder.encode(password)));
		} else if (existing.get().getRole() != Role.ADMIN) {
			existing.get().setRole(Role.ADMIN);
			userRepository.save(existing.get());
		}
	}
}
