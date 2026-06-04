package se.sundsvall.notifier.users.api.model;

import java.util.Objects;

public class JwtResponse {

	private String token;

	public JwtResponse() {}

	public JwtResponse(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public int hashCode() {
		return Objects.hash(token);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		JwtResponse other = (JwtResponse) obj;
		return Objects.equals(token, other.token);
	}
}
