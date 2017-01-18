package dk.os2opgavefordeler.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

import dk.os2opgavefordeler.model.User;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import javax.persistence.Query;

@Repository(forEntity = User.class)
@Transactional
public abstract class UserRepository extends AbstractEntityRepository<User, Long> {

	public abstract User findByEmail(String email);

	public boolean hasEmployment(Long userId, Long employmentId){
		if(userId == null || employmentId == null) { return false; }
		Query hasEmploymentQuery = entityManager().createQuery("select user from User user left join user.roles as role where user.id = :userId and role.employment.id = :employmentId");
		hasEmploymentQuery.setParameter("userId", userId);
		hasEmploymentQuery.setParameter("employmentId", employmentId);
		return (!hasEmploymentQuery.getResultList().isEmpty());
	}

}