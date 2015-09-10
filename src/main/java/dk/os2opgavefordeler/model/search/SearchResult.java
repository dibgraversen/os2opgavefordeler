package dk.os2opgavefordeler.model.search;

import com.google.common.base.MoreObjects;
import java.util.List;

/**
 * @author hlo@miracle.dk
 */
public class SearchResult <T> {
	private long totalMatches;
	private List<T> results;

	public SearchResult() {
	}

	public long getTotalMatches() {
		return totalMatches;
	}

	public void setTotalMatches(long totalMatches) {
		this.totalMatches = totalMatches;
	}

	public List<T> getResults() {
		return results;
	}

	public void setResults(List<T> results) {
		this.results = results;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("totalMatches", totalMatches)
				.add("results", results)
				.toString();
	}
}
