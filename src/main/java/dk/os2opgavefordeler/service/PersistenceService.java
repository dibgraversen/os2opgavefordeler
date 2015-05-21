package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.kle.KleMainGroup;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

public interface PersistenceService {
	List<KleMainGroup> fetchAllKleMainGroups();
	KleMainGroup fetchMainGroup(String number);

	void storeAllKleMainGroups(List<KleMainGroup> groups);

	<T> void persist(T entity);
	<T> List<T> criteriaFind(Class clazz, CriteriaOp op);


	interface CriteriaOp<T> {
		void apply(CriteriaBuilder cb, CriteriaQuery<T> cq);
	}
}
