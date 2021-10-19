package io.roach.bank.repository.jpa;

import java.sql.Types;

import org.hibernate.dialect.identity.IdentityColumnSupportImpl;

public class CockroachDBIdentityColumnSupport extends IdentityColumnSupportImpl {
    @Override
    public boolean supportsIdentityColumns() {
        return true;
    }

    @Override
    public String getIdentitySelectString(String table, String column, int type) {
        switch (type) {
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.NUMERIC:
                return "select unique_rowid()";
            default:
                return "select gen_random_uuid()";
        }
    }

    @Override
    public String getIdentityColumnString(int type) {
        switch (type) {
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.NUMERIC:
                return "not null default unique_rowid()";
            default:
                return "not null default gen_random_uuid()";
        }
    }

    @Override
    public boolean hasDataTypeInIdentityColumn() {
        return true;
    }
}
