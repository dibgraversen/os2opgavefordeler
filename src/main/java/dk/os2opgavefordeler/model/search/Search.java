package dk.os2opgavefordeler.model.search;

import com.google.common.base.MoreObjects;

/**
 * @author hlo@miracle.dk
 */
public class Search {
	private int offset;
	private int pageSize;
	private long municipalityId;

	public Search() {
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public long getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(long municipalityId) {
		this.municipalityId = municipalityId;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("offset", offset)
				.add("pageSize", pageSize)
				.add("municipality", municipalityId)
				.toString();
	}
}
