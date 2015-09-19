package com.quantasnet.gitshark.config;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class WebSecurityConfigTest {

	final Pattern pattern = Pattern.compile(WebSecurityConfig.GIT_HTTP_REGEX);
	
	@Test
	public void testGitRepoRegexMatches() {
		Assert.assertTrue(pattern.matcher("/repo/user/gitserver.git").matches());
	}
	
	@Test
	public void testGitRepoRegexMatchesParams() {
		Assert.assertTrue(pattern.matcher("/repo/user/gitserver.git?service=git-receive-pack").matches());
	}
	
	@Test
	public void testGitRepoRegexDoesntMatchGitIgnore() {
		Assert.assertFalse(pattern.matcher("/repo/user/gitserver/tree/master/.gitignore").matches());
	}
	
	@Test
	public void testGitRepoRegexDoesntMatch() {
		Assert.assertFalse(pattern.matcher("/repo/user/gitserver/tree/master/blah").matches());
	}
	
}
