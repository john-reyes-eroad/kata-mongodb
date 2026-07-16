package com.example.mongocrud;

import org.hibernate.validator.internal.util.logging.Log_$logger;
import org.hibernate.validator.internal.util.logging.Messages_$bundle;
import org.hibernate.validator.internal.constraintvalidators.bv.NotBlankValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForBigDecimal;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public final class HibernateValidatorRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Hibernate Validator resolves these generated implementations by name at runtime.
        hints.reflection().registerType(Log_$logger.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(Messages_$bundle.class, MemberCategory.ACCESS_DECLARED_FIELDS);
        hints.reflection().registerType(NotBlankValidator.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(NotNullValidator.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(MinValidatorForInteger.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(MaxValidatorForInteger.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(DecimalMinValidatorForBigDecimal.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(DecimalMaxValidatorForBigDecimal.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(PositiveOrZeroValidatorForBigDecimal.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
    }
}
