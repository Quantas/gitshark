package com.quantasnet.gitserver.git.ui;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.tika.Tika;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.ImmutableSet;
import com.quantasnet.gitserver.git.exception.GitServerErrorException;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.model.Breadcrumb;
import com.quantasnet.gitserver.git.model.Commit;
import com.quantasnet.gitserver.git.model.RepoFile;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.git.service.ReadmeFileService;
import com.quantasnet.gitserver.git.service.RepositoryUtilities;

@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class TreeController {

	private static final Set<MediaType> ADDITIONAL_TYPES = new ImmutableSet.Builder<MediaType>()
			.add(MediaType.APPLICATION_XML)
			.add(MediaType.APPLICATION_JSON)
			.add(MediaType.APPLICATION_XHTML_XML)
			.build();
	
	@Autowired
	private RepositoryUtilities repoUtils;
	
	@Autowired
	private ReadmeFileService readmeService;
	
	@RequestMapping("/tree")
	public String displayRepoTreeNoBranch(final GitRepository repo, @PathVariable final String repoOwner, @PathVariable final String repoName, final Model model, final HttpServletRequest req) throws GitServerException {
		final StringBuilder builder = new StringBuilder();
		repo.execute(db -> {
			try {
				builder.append(db.getBranch());
			} catch (final Exception e) {
				throw new GitServerErrorException(e);
			}
		});
		return displayRepoTree(repo, repoOwner, repoName, builder.toString(), false, model, req);
	}
	
	@RequestMapping("/tree/{branch}/**")
	public String displayRepoTree(final GitRepository repo, @PathVariable final String repoOwner, @PathVariable final String repoName, @PathVariable final String branch, @RequestParam(required = false) final boolean file, final Model model, final HttpServletRequest req) throws GitServerException {
		final String repoPath = "/repo/" + repoOwner + '/' + repoName + "/tree/" + branch + '/';
		final String path = repoUtils.resolvePath(req, repoPath, branch);
		
		model.addAttribute("branch", branch);
		model.addAttribute("breadcrumbs", Breadcrumb.generateBreadcrumbs(req.getContextPath(), repoName, repoPath, path));
		
		repo.execute(db -> {
			if (repo.hasCommits()) {
				repoUtils.addRefsToModel(model, db);
				
				final RevCommit commit = repoUtils.getRefHeadCommit(branch, db);
				model.addAttribute("lastCommit", new Commit(commit, repo));
				
				if (file) {
					final RepoFile repoFile = repoUtils.getFileToDisplay(repo, db, branch, path);
					
					final Tika tika = new Tika();
					final String mediaType = tika.detect(new ByteArrayInputStream(repoFile.getFileContentsRaw()));
					final MediaType type = MediaType.parseMediaType(mediaType);
					
					if ("image".equals(type.getType())) {
						model.addAttribute("mediaType", mediaType);
						model.addAttribute("base64contents", Base64.getEncoder().encodeToString(repoFile.getFileContentsRaw()));
					} else if (!"text".equals(type.getType()) && !ADDITIONAL_TYPES.contains(type)) {
						model.addAttribute("rawError", "Cannot display file.");
					}
					
					model.addAttribute("file", repoFile);
				} else {
					final List<RepoFile> files = repoUtils.getFiles(repo, db, branch, path, false);
					model.addAttribute("readme", readmeService.resolveReadMeFile(repo, db, files));
					model.addAttribute("files", files);
				}
			}
		});
		
		if (file) {
			return "git/file";
		}
		return "git/tree";
	}
}