package se.sundsvall.notifier.csvimport.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmployeeDTO {

	@JsonProperty("PersonId")
	public String personId;

	@JsonProperty("Givenname")
	public String givenName;

	@JsonProperty("Lastname")
	public String lastName;

	@JsonProperty("WorkMobile")
	public String workMobile;

	@JsonProperty("WorkPhone")
	public String workPhone;

	@JsonProperty("Title")
	public String title;

	@JsonProperty("OrgId")
	public String orgId;

	@JsonProperty("PrimaryEMailAddress")
	public String primaryEmailAddress;

	@JsonProperty("ManagerId")
	public String managerId;

	@JsonProperty("ManagerCode")
	public String managerCode;
}
