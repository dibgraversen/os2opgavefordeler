package dk.osto.model;

/**
 * @author hlo@miracle.dk
 */
public class TaskRoute {
	private Long id;
	private String name;

	public TaskRoute() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "TaskRoute{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}
