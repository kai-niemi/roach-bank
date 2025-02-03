package io.roach.bank.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Service;

/**
 * Indicates the annotated class is a coarse-grained service facade and transaction boundary.
 * Its architectural role is to delegate to control services or repositories to perform
 * actual business logic processing in the context of a new transaction.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Service
public @interface ServiceFacade {
}
