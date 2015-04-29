package dk.osto.model.presentation;

import dk.osto.model.KLE;

/**
 * A Presentation Object for the TopicRoute model ready for use in the UI.
 * @author hlo@miracle.dk
 */
public class TopicRoutePO {

	/**
	 * Id of the topic route for identification.
	 */
	private int id;

	/**
	 * Specifies parent KLE id.
	 */
	private int parent;

	/**
	 * The KLE data associated with this.
	 */
	private KLE kle;

	/**
	 * The organisational unit responsible for handling this topic.
	 */
	private int org;

	/**
	 * Employee responsible for handling this topic.
	 */
	private int employee;

	/**
	 * The owner of the topic, who can decide who handles the topic.
	 */
	private int responsible;

	public TopicRoutePO() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getParent() {
		return parent;
	}

	public void setParent(int parent) {
		this.parent = parent;
	}

	public KLE getKle() {
		return kle;
	}

	public void setKle(KLE kle) {
		this.kle = kle;
	}

	public int getOrg() {
		return org;
	}

	public void setOrg(int org) {
		this.org = org;
	}

	public int getEmployee() {
		return employee;
	}

	public void setEmployee(int employee) {
		this.employee = employee;
	}

	public int getResponsible() {
		return responsible;
	}

	public void setResponsible(int responsible) {
		this.responsible = responsible;
	}
}
