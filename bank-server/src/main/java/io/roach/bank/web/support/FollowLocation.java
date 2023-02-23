package io.roach.bank.web.support;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public abstract class FollowLocation {
    private FollowLocation() {
    }

    public static boolean ofCurrentRequest() {
        HttpServletRequest currentRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest();
        return ServletRequestUtils.getBooleanParameter(currentRequest, "followLocation", false);
    }
}
