package com.quantasnet.gitshark.git.ui;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.model.RefHolder;
import com.quantasnet.gitshark.git.model.RefType;
import com.quantasnet.gitshark.git.repo.GitRepository;
import com.quantasnet.gitshark.git.service.RefService;
import com.quantasnet.gitshark.git.service.RepositoryUtilities;

@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class RefController {

	private static final Logger LOG = LoggerFactory.getLogger(RefController.class);

	@Autowired
	private RepositoryUtilities repoUtils;
	
	@Autowired
	private RefService refService;
	
	@RequestMapping(value = "/{refType:(?:branch|tag)}", method = RequestMethod.GET)
	public String refs(final GitRepository repo, @PathVariable final String refType, final Model model) throws GitSharkException {
		
		final RefType type = RefType.getForName(refType);
		
		repo.execute(db -> {
			final Map<String, Ref> branchRefs =  type == RefType.BRANCH ? refService.branches(repo) : refService.tags(repo);

			final List<RefHolder> refs = branchRefs.keySet()
					.stream()
					.map(entry -> {
						try {
							return new RefHolder(repoUtils.getRefHeadCommit(entry, repo, db), repo, entry);
						} catch (GitSharkException e) {
							LOG.error("Error building list of refs for {}, type={}", repo.getFullDisplayName(), type, e);
						}
						return null;
					})
					.sorted()
					.collect(Collectors.toList());

			model.addAttribute("refType", type.getName());
			model.addAttribute("refTitle", StringUtils.capitalize(type.getName()));
			model.addAttribute("sectionTitle", type.getTitle());
			model.addAttribute("refs", refs);
		});
		
		return "git/refs";
	}
}
