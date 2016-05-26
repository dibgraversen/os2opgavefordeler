package dk.os2opgavefordeler.distribution;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

import dk.os2opgavefordeler.model.*;

@Repository(forEntity = DistributionRule.class)
public abstract class DistributionRuleRepository extends AbstractEntityRepository<DistributionRule, Long> {

    public abstract Iterable<DistributionRule> findByKleAndMunicipality(Kle kle, Municipality municipality);

}