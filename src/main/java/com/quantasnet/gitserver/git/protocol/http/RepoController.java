package com.quantasnet.gitserver.git.protocol.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefAdvertiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.git.service.FilesystemRepositoryService;

@RequestMapping("/repo/{repoOwner}/{repoName}.git")
@Controller
public class RepoController {

	@Autowired
	private FilesystemRepositoryService repoService;
	
	@RequestMapping(value = "/" + Constants.HEAD, method = RequestMethod.GET)
	public ResponseEntity<byte[]> head(final GitRepository repo) throws GitServerException {
		final byte[] head = repo.executeWithReturn(db -> {
			return ("ref: " + db.getFullBranch()).getBytes();
		});		
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

			final Map<String, Ref> refs = new HashMap<>();
			refs.putAll(repoService.branches(repo));
			refs.putAll(repoService.tags(repo));
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