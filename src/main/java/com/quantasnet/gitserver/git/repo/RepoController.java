package com.quantasnet.gitserver.git.repo;

import static org.eclipse.jgit.lib.RefDatabase.ALL;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefAdvertiser;
import org.eclipse.jgit.util.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class RepoController {

	private static final Logger LOG = LoggerFactory.getLogger(RepoController.class);
	
	@RequestMapping(value = "/" + Constants.HEAD, method = RequestMethod.GET)
	public ResponseEntity<byte[]> head(final GitRepository repo) throws IOException {
		LOG.debug("HEAD requested");
		final byte[] head = IO.readFully(new File(repo.getFullRepoDirectory(), Constants.HEAD));
		return new ResponseEntity<byte[]>(head, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/info/refs", method = RequestMethod.GET, produces = "text/plain")
	public ResponseEntity<String> infoRefs(final GitRepository repo) throws Exception {
		LOG.debug("info/refs requested");
		
		final StringBuilder output = new StringBuilder();
		
		GitRepository.execute(repo, db -> {
			final RefAdvertiser adv = new RefAdvertiser() {
				@Override
				protected void writeOne(final CharSequence line) throws IOException {
					output.append(line.toString().replace(' ', '\t'));
				}

				@Override
				protected void end() {
				}
			};
			
			adv.init(db);
			adv.setDerefTags(true);

			final Map<String, Ref> refs = db.getRefDatabase().getRefs(ALL);
			refs.remove(Constants.HEAD);
			adv.send(refs);
		});
		
		return new ResponseEntity<String>(output.toString(), HttpStatus.OK);
	}
	
	@RequestMapping("/**")
	public ResponseEntity<Object> repo(final GitRepository repo, final HttpServletRequest req) {
		LOG.info("Path requested ={}, {}", req.getMethod(), req.getServletPath());
		LOG.info("Parameters = {}", req.getParameterMap());
		return new ResponseEntity<Object>("Fail", HttpStatus.OK);
	}
	
}
