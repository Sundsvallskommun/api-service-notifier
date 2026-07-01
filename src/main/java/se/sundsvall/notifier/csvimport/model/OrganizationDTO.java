package se.sundsvall.notifier.csvimport.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrganizationDTO {

	@JsonProperty("CompanyId")
	public String companyId;

	@JsonProperty("OrgId")
	public String orgId;

	@JsonProperty("OrgName")
	public String orgName;

	@JsonProperty("ParentId")
	public String parentId;

	@JsonProperty("TreeLevel")
	public String treeLevel;
}
