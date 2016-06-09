package dk.os2opgavefordeler.distribution;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

import dk.os2opgavefordeler.model.DistributionRuleFilterName;

@Repository(forEntity = DistributionRuleFilterName.class)
public abstract class DistributionRuleFilterNameRepository extends AbstractEntityRepository<DistributionRuleFilterName, Long> {


}