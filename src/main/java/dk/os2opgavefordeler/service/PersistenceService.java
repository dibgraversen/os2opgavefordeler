package dk.os2opgavefordeler.service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public interface PersistenceService {
	<T> void persist(T entity);
	<T> T merge(T entity);

	<T> List<T> criteriaFind(Class<T> clazz, CriteriaOp op);
	<T> List<T> findAll(Class<T> clazz);

	void rollbackTransaction();

	@FunctionalInterface
	interface CriteriaOp<T> {
		void apply(CriteriaBuilder cb, CriteriaQuery<T> cq, Root<T> ent);
	}

	EntityManager getEm();
}
