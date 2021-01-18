package io.roach.bank.aspect;

import java.lang.annotation.Annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.annotation.AnnotationUtils;

abstract class AopSupport {
    private AopSupport() {
    }

    public static <A extends Annotation> A findAnnotation(ProceedingJoinPoint pjp, Class<A> annotationType) {
        return AnnotationUtils
                .findAnnotation(pjp.getSignature().getDeclaringType(), annotationType);
    }
}
