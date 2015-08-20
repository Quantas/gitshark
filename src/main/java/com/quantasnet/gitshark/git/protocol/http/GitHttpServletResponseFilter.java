package com.quantasnet.gitshark.git.protocol.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * This class is necessary to prevent ";charset=WHATEVER" from being
 * appended to the "Content-Type" headers for Git HTTP requests.
 */
public class GitHttpServletResponseFilter implements Filter {

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		chain.doFilter(request, new GitHttpServletResponseWrapper((HttpServletResponse) response));
	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		// nothing here
	}

	@Override
	public void destroy() {
		// nothing here
	}
	
	public static class GitHttpServletResponseWrapper extends HttpServletResponseWrapper {
		
		public GitHttpServletResponseWrapper(final HttpServletResponse response) {
			super(response);
		}
		
		@Override
		public void setCharacterEncoding(final String type) {
			// nothing here
		}
	}
}