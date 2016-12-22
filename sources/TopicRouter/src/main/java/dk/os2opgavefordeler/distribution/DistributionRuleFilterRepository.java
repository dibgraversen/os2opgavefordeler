package dk.os2opgavefordeler.distribution;

import dk.os2opgavefordeler.model.DistributionRuleFilter;
import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

/**
 * @author hlo@miracle.dk
 */
@Repository(forEntity = DistributionRuleFilter.class)
@Transactional
public abstract class DistributionRuleFilterRepository extends AbstractEntityRepository<DistributionRuleFilter, Long> {
}
