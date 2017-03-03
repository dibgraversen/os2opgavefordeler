package dk.os2opgavefordeler.model.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience class for a KLE result from an REST call
 *
 * @author psu@digital-identity.dk
 */
public class KleRestResultPO {

	/**
	 * KLE number e.g. 01.06.01.
	 */
	private String number;

	/**
	 * KLE name.
	 */
	private String serviceText;
	
	/**
	 * Children KLEs
	 */
	private List<KleRestResultPO> children;

	public KleRestResultPO() {
		children = new ArrayList<>();
	}	
	
	public KleRestResultPO(String number, String serviceText) {
		this();
		this.number = number;
		this.serviceText = serviceText;
	}
	
	public KleRestResultPO(String number, String serviceText, List<KleRestResultPO> children) {
		this();
		this.number = number;
		this.serviceText = serviceText;
		this.children = children;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getServiceText() {
		return serviceText;
	}

	public void setServiceText(String serviceText) {
		this.serviceText = serviceText;
	}

	public List<KleRestResultPO> getChildren() {
		return children;
	}

	public void setChildren(List<KleRestResultPO> children) {
		this.children = children;
	}
	
}

