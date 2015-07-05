package com.quantasnet.gitserver.jgit.vendor;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.util.ClassUtils;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

/**
 * Custom extension of the ShallowEtagHeaderFilter that 
 * adds the ETag header to GET and HEAD requests.
 */
public class EtagHeaderFilter extends ShallowEtagHeaderFilter {

	private static final String HEADER_CACHE_CONTROL = "Cache-Control";
	private static final String DIRECTIVE_NO_STORE = "no-store";
	private static final Set<String> SUPPORTED_METHODS = new HashSet<String>(Arrays.asList(HttpMethod.GET.name(), HttpMethod.HEAD.name()));
	
	/** 
	 * Checking for Servlet 3.0+ HttpServletResponse.getHeader(String)
	 */
	private static final boolean RESPONSE_GET_HEADER_AVAILABLE = ClassUtils.hasMethod(HttpServletResponse.class, "getHeader", String.class);
	
	@Override
	protected boolean isEligibleForEtag(HttpServletRequest request, HttpServletResponse response, int responseStatusCode, InputStream inputStream) {

		if (responseStatusCode >= 200 && responseStatusCode < 300 &&
				SUPPORTED_METHODS.contains(request.getMethod())) {
			String cacheControl = RESPONSE_GET_HEADER_AVAILABLE ? response.getHeader(HEADER_CACHE_CONTROL) : null;
			if (cacheControl == null || !cacheControl.contains(DIRECTIVE_NO_STORE)) {
				return true;
			}
		}
		return false;
	}
}