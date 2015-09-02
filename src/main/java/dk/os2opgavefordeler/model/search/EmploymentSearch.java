package dk.os2opgavefordeler.model.search;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;

import javax.inject.Inject;

/**
 * @author hlo@miracle.dk
 */
public class EmploymentSearch extends Search {

	@Inject
	Logger log;

	private String nameTerm;

	private String initialsTerm;

	public EmploymentSearch() {
	}

	public String getNameTerm() {
		return nameTerm;
	}

	public void setNameTerm(String nameTerm) {
		this.nameTerm = nameTerm;
	}

	public String getInitialsTerm() {
		return initialsTerm;
	}

	public void setInitialsTerm(String initialsTerm) {
		this.initialsTerm = initialsTerm;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("nameTerm", nameTerm)
				.add("initialsTerm", initialsTerm)
				.toString();
	}
}
