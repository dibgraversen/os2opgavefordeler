package dk.os2opgavefordeler.model;

import java.util.List;
import java.util.stream.Stream;

public interface IHasChildren<T extends IHasChildren<T>> {
	List<T> getChildren();

	default Stream<T> flattened() {
		return flatten((T) this);
	}

	default Stream<T> flatten(T child) {
		return Stream.concat(
			Stream.of(child),
			child.getChildren().stream().flatMap(this::flatten)
		);
	}
}
