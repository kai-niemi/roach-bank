package io.roach.bank.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A meta-annotation including @Transactional, indicating that the annotated class or method
 * must execute non-transactional, hence Propagation.NOT_SUPPORTED.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public @interface TransactionImplicit {
    @AliasFor(annotation = Transactional.class, attribute = "readOnly")
    boolean readOnly() default false;
}
