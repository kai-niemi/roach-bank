package io.roach.bank.web.support;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@WebFilter("/api")
public class ResponseHeaderFilter extends OncePerRequestFilter {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String HEADER_NAME = "X-Application-Context";

    public static final String HEADER_VERSION = "X-Application-Version";

    @Value("${info.build.name}")
    private String name;

    @Value("${info.build.version}")
    private String version;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
//        Enumeration<String> h = request.getHeaderNames();
//        logger.debug("{} {}", request.getMethod(), request.getRequestURI());
//        while (h.hasMoreElements()) {
//            String s = h.nextElement();
//            logger.debug("  {}: {}", s, request.getHeader(s));
//        }
        response.setHeader(HEADER_NAME, name);
        response.setHeader(HEADER_VERSION, version);

        filterChain.doFilter(request, response);
    }
}
