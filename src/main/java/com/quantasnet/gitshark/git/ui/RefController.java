package com.quantasnet.gitshark.git.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.Ref;
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
import com.quantasnet.gitshark.git.service.FilesystemRepositoryService;
import com.quantasnet.gitshark.git.service.RepositoryUtilities;

@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class RefController {

	@Autowired
	private RepositoryUtilities repoUtils;
	
	@Autowired
	private FilesystemRepositoryService repoService;
	
	@RequestMapping(value = "/{refType:(?:branch|tag)}", method = RequestMethod.GET)
	public String refs(final GitRepository repo, @PathVariable final String refType, final Model model) throws GitSharkException {
		
		final RefType type = RefType.getForName(refType);
		
		repo.execute(db -> {
			final List<RefHolder> refs = new ArrayList<>();
			
			final Map<String, Ref> branchRefs =  type == RefType.BRANCH ? repoService.branches(repo) : repoService.tags(repo);
			for (final String entry : branchRefs.keySet()) {
				refs.add(new RefHolder(repoUtils.getRefHeadCommit(entry, repo, db), repo, entry));
			}
			
			Collections.sort(refs);
			
			model.addAttribute("refType", type.getName());
			model.addAttribute("refTitle", StringUtils.capitalize(type.getName()));
			model.addAttribute("sectionTitle", type.getTitle());
			model.addAttribute("refs", refs);
		});
		
		return "git/refs";
	}
}