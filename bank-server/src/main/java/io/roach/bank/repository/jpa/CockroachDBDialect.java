package io.roach.bank.repository.jpa;

import org.hibernate.dialect.CockroachDialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.type.StandardBasicTypes;

public class CockroachDBDialect extends CockroachDialect {
    public CockroachDBDialect() {
//        registerFunction("cluster_logical_timestamp",
//                new SQLFunctionTemplate(StandardBasicTypes.BIG_DECIMAL, "cluster_logical_timestamp()"));
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new CockroachDBIdentityColumnSupport();
    }
}
