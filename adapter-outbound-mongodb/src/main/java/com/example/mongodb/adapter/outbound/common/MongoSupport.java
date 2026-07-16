package com.example.mongodb.adapter.outbound.common;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

public final class MongoSupport {

    private MongoSupport() {
    }

    public static ObjectId parseObjectId(String id) {
        if (id == null || id.isBlank() || !ObjectId.isValid(id)) {
            return null;
        }
        return new ObjectId(id);
    }

    public static Date toDate(Instant value) {
        return value == null ? null : Date.from(value);
    }

    public static Instant toInstant(Date value) {
        return value == null ? null : value.toInstant();
    }

    public static Decimal128 toDecimal128(BigDecimal value) {
        return value == null ? null : new Decimal128(value);
    }

    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Decimal128 decimal128) {
            return decimal128.bigDecimalValue();
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(value.toString());
    }
}
