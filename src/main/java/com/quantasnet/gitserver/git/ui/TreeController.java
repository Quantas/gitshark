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
	public String displayRepoTreeNoBranch(final GitRepository repo, final Model model, final HttpServletRequest req) throws GitServerException {
		final String ref = repo.executeWithReturn(db -> {
			try {
				return db.getBranch();
			} catch (final Exception e) {
				throw new GitServerErrorException(e);
			}
		});
		return displayRepoTree(repo, ref, model, req);
	}
	
	@RequestMapping("/tree/{ref}/**")
	public String displayRepoTree(final GitRepository repo, @PathVariable final String ref, final Model model, final HttpServletRequest req) throws GitServerException {
		final String repoPath = "/repo/" + repo.getInterfaceBaseUrl() + "/tree/" + ref + '/';
		final String path = repoUtils.resolvePath(req, repoPath, ref);
		
		model.addAttribute("branch", ref);
		model.addAttribute("breadcrumbs", Breadcrumb.generateBreadcrumbs(req.getContextPath(), repo.getDisplayName(), repoPath, path));
		
		return repo.executeWithReturn(db -> {
			if (repo.hasCommits()) {
				repoUtils.addRefsToModel(model, db);
				
				final RevCommit commit = repoUtils.getRefHeadCommit(ref, db);
				model.addAttribute("lastCommit", new Commit(commit, repo));

				final List<RepoFile> files = repoUtils.getFiles(repo, db, ref, path, true);

				boolean file = false;

				if (!files.get(0).isDirectory()) {
					file = true;
				}

				if (file) {
					final RepoFile repoFile = files.get(0);

					if (null != repoFile) {
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
						return "git/file";
					}
				} else {
					if (repoUtils.isPathRoot(path)) {
						model.addAttribute("readme", readmeService.resolveReadMeFile(repo, db, ref, files));
					}

					model.addAttribute("files", files);
				}
			}
			return "git/tree";
		});
	}
}