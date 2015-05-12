package dk.os2opgavefordeler.model.presentation;

/**
 * @author hlo@miracle.dk
 */
public class UserSettingsPO {
	private FilterScope scope;
	private boolean showResponsible;
	private boolean showExpandedOrg;

	public UserSettingsPO() {
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
				"scope=" + scope +
				", showResponsible=" + showResponsible +
				", showExpandedOrg=" + showExpandedOrg +
				'}';
	}
}
