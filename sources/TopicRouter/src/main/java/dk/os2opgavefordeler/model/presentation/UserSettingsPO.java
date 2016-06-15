package dk.os2opgavefordeler.model.presentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.os2opgavefordeler.model.UserSettings;

/**
 * @author hlo@miracle.dk
 */
public class UserSettingsPO {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private long id;
	private long userId;
	private FilterScope scope;

	private boolean showResponsible;
	private boolean showExpandedOrg;

	public UserSettingsPO() {
	}

	public UserSettingsPO(UserSettings settings) {
		id = settings.getId();
		userId = settings.getUserId();
		scope = settings.getScope();
		showResponsible = settings.isShowResponsible();
		showExpandedOrg = settings.isShowExpandedOrg();
	}

	public UserSettings asUserSettings(){
		UserSettings result = new UserSettings();

		result.setId(id);
		result.setUserId(userId);
		result.setScope(scope);
		result.setShowResponsible(showResponsible);
		result.setShowExpandedOrg(showExpandedOrg);

		return result;
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
		return "UserSettingsPO{" +
				"id=" + id +
				", userId=" + userId +
				", scope=" + scope +
				", showResponsible=" + showResponsible +
				", showExpandedOrg=" + showExpandedOrg +
				'}';
	}
}
