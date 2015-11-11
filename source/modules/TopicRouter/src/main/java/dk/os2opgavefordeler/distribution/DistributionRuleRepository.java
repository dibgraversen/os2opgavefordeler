package dk.os2opgavefordeler.distribution;

import dk.os2opgavefordeler.model.*;
import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;


@Repository(forEntity = DistributionRule.class)
public abstract class DistributionRuleRepository extends AbstractEntityRepository<DistributionRule, Long> {

    public abstract Iterable<DistributionRule> findByKleAndMunicipality(Kle kle, Municipality municipality);

}