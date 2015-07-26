package com.quantasnet.gitserver.git.protocol.http;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.transport.RefAdvertiser;
import org.eclipse.jgit.util.IO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.repo.GitRepository;

@RequestMapping("/repo/{repoOwner}/{repoName}.git")
@Controller
public class RepoController {

	@RequestMapping(value = "/" + Constants.HEAD, method = RequestMethod.GET)
	public ResponseEntity<byte[]> head(final GitRepository repo) throws IOException {
		final byte[] head = IO.readFully(new File(repo.getFullRepoDirectory(), Constants.HEAD));
		return new ResponseEntity<>(head, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/info/refs", method = RequestMethod.GET, produces = Constants.TEXT_PLAIN)
	public ResponseEntity<String> infoRefs(final GitRepository repo) throws GitServerException {
		final StringBuilder output = new StringBuilder();
		
		repo.execute(db -> {
			final RefAdvertiser adv = new RefAdvertiser() {
				@Override
				protected void writeOne(final CharSequence line) throws IOException {
					output.append(line.toString().replace(' ', '\t'));
				}

				@Override
				protected void end() {
					// nothing here
				}
			};
			
			adv.init(db);
			adv.setDerefTags(true);

			final Map<String, Ref> refs = db.getRefDatabase().getRefs(RefDatabase.ALL);
			refs.remove(Constants.HEAD);
			adv.send(refs);
		});
		
		return new ResponseEntity<>(output.toString(), HttpStatus.OK);
	}
	
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String repo(@PathVariable final String repoOwner, @PathVariable final String repoName) {
		return "redirect:/repo/" + repoOwner + '/' + repoName;
	}
}