package se.sundsvall.notifier.users.service.mapper;

import java.util.Optional;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.util.MunicipalityUtils;
import se.sundsvall.notifier.users.api.model.UserRequest;
import se.sundsvall.notifier.users.api.model.UserResponse;
import se.sundsvall.notifier.users.integration.db.model.UserEntity;
import se.sundsvall.notifier.users.integration.db.model.enums.Role;
import se.sundsvall.notifier.users.integration.db.model.enums.Status;

@Component
public class UserMapper {

	public UserResponse toUserResponse(final UserEntity user) {
		return Optional.ofNullable(user)
			.map(entity -> UserResponse.create()
				.withEmail(entity.getEmail())
				.withId(entity.getId())
				.withPhoneNumber(entity.getPhoneNumber())
				.withMunicipalityName(Optional.ofNullable(MunicipalityUtils.findById(entity.getMunicipalityId()))
					.map(MunicipalityUtils.Municipality::name)
					.orElse(null))
				.withStatus(String.valueOf(entity.getStatus()))
				.withRole(Optional.of(entity.getRole()).map(Enum::name).orElse(null)))
			.orElse(null);
	}

	public UserEntity toUserEntity(UserRequest userRequest, String encryptedPassword) {
		return Optional.ofNullable(userRequest)
			.map(request -> UserEntity.create()
				.withEmail(request.getEmail())
				.withPhoneNumber(request.getPhoneNumber())
				.withMunicipalityId(resolveMunicipalityId(request.getMunicipalityName()))
				.withPassword(encryptedPassword)
				.withStatus(Status.valueOf(request.getStatus().toUpperCase()))
				.withRole(Optional.of(request.getRole()).map(s -> Role.valueOf(s.toUpperCase())).orElse(Role.USER)))
			.orElse(null);
	}

	public String resolveMunicipalityId(final String input) {
		if (MunicipalityUtils.existsById(input)) {
			return input;
		}
		return MunicipalityUtils.findByName(input).id();
	}
}
