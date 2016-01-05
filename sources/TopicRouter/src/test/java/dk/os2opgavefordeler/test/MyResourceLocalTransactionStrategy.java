package dk.os2opgavefordeler.test;

import org.apache.deltaspike.jpa.impl.transaction.ResourceLocalTransactionStrategy;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

/**
 * Created by kjaer on 11/4/15.
 */
@Dependent
@Alternative
public class MyResourceLocalTransactionStrategy extends ResourceLocalTransactionStrategy {
}
