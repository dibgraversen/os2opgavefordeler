package dk.os2opgavefordeler.model;

import dk.os2opgavefordeler.model.presentation.FilterScope;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author hlo@miracle.dk
 */
@Entity
public class UserSettings implements Serializable {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private long userId;

	@Enumerated(EnumType.STRING)
	private FilterScope scope = FilterScope.ALL;

	private boolean showResponsible;

	private boolean showExpandedOrg;

	public UserSettings() {
	}

	public UserSettings(long userId) {
		this.userId = userId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public FilterScope getScope() {
		return scope;
	}

	public void setScope(FilterScope scope) {
		this.scope = scope;
	}

	public boolean isShowResponsible() {
		return showResponsible;
	}

	public void setShowResponsible(boolean showResponsible) {
		this.showResponsible = showResponsible;
	}

	public boolean isShowExpandedOrg() {
		return showExpandedOrg;
	}

	public void setShowExpandedOrg(boolean showExpandedOrg) {
		this.showExpandedOrg = showExpandedOrg;
	}

	@Override
	public String toString() {
		return "UserSettings{" +
				"id=" + id +
				", userId=" + userId +
				", scope=" + scope +
				", showResponsible=" + showResponsible +
				", showExpandedOrg=" + showExpandedOrg +
				'}';
	}
}
