package com.quantasnet.gitserver.git.ui;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.tika.Tika;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableSet;
import com.quantasnet.gitserver.git.exception.GitServerErrorException;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.model.Breadcrumb;
import com.quantasnet.gitserver.git.model.Commit;
import com.quantasnet.gitserver.git.model.ReadmeFile;
import com.quantasnet.gitserver.git.model.RepoFile;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.git.service.RepositoryUtilities;
import com.quantasnet.gitserver.git.service.SpecialMarkupService;

@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class TreeController {

	private static final String SPECIAL_MARKUP = "specialmarkup";
	private static final String HISTORY = "history";

	private static final Set<MediaType> ADDITIONAL_TYPES = new ImmutableSet.Builder<MediaType>()
			.add(MediaType.APPLICATION_XML)
			.add(MediaType.APPLICATION_JSON)
			.add(MediaType.APPLICATION_XHTML_XML)
			.build();
	
	@Autowired
	private RepositoryUtilities repoUtils;
	
	@Autowired
	private SpecialMarkupService specialMarkupService;

	@Autowired
	private ServletContext servletContext;
	
	@RequestMapping("/tree")
	public Object displayRepoTreeNoBranch(final GitRepository repo, final Model model, final HttpServletRequest req) throws GitServerException {
		final String ref = repo.executeWithReturn(db -> {
			try {
				return db.getBranch();
			} catch (final Exception e) {
				throw new GitServerErrorException(e);
			}
		});
		return displayRepoTree(repo, "tree", ref, model, req);
	}
	
	@RequestMapping("/{type:(?:tree|raw|history)}/{ref}/**")
	public Object displayRepoTree(final GitRepository repo, @PathVariable final String type, @PathVariable final String ref, final Model model, final HttpServletRequest req) throws GitServerException {
		final String repoPath = "/repo/" + repo.getInterfaceBaseUrl() + "/tree/" + ref + '/';
		final String path = repoUtils.resolvePath(req, repoPath, ref);
		
		final String breadCrumbsPath = HISTORY.equals(type) ? repoPath.replaceFirst("\\/tree\\/", "/history/") : repoPath;
		
		model.addAttribute("branch", ref);
		model.addAttribute("breadcrumbs", Breadcrumb.generateBreadcrumbs(servletContext.getContextPath(), repo.getDisplayName(), breadCrumbsPath, path));
		
		return repo.executeWithReturn(db -> {
			if (repo.hasCommits()) {
				repoUtils.addRefsToModel(model, db);
				
				final RevCommit commit = repoUtils.getRefHeadCommit(ref, db);
				model.addAttribute("lastCommit", new Commit(commit, repo));

				final List<RepoFile> files = repoUtils.getFiles(repo, db, ref, path);

				boolean file = false;

				if (!files.get(0).isDirectory()) {
					file = true;
				}
				
				if (HISTORY.equals(type)) {
					try {
						final List<Commit> history = new ArrayList<>();
						final LogCommand logCommand = Git.wrap(db).log().setMaxCount(50);

						if (path.length() > 1) {
							logCommand.addPath(path);
						}

						for (final RevCommit historyCommit : logCommand.call()) {
							history.add(new Commit(historyCommit, repo));
						}
						model.addAttribute("historyPos", ref);
						model.addAttribute(HISTORY, history);
						return "git/history";
					} catch (Exception e) {
						throw new GitServerErrorException(e);
					}
				}

				if (file) {
					final RepoFile repoFile = files.get(0);

					if ("raw".equals(type)) {
						final HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.TEXT_PLAIN);
						return new ResponseEntity<>(new String(repoFile.getFileContentsRaw()), headers, HttpStatus.OK);
					}

					if (null != repoFile) {
						final Tika tika = new Tika();
						final String mediaTypeString = tika.detect(new ByteArrayInputStream(repoFile.getFileContentsRaw()));
						final MediaType mediaType = MediaType.parseMediaType(mediaTypeString);

						if ("image".equals(mediaType.getType())) {
							model.addAttribute("mediaType", mediaTypeString);
							model.addAttribute("base64contents", Base64.getEncoder().encodeToString(repoFile.getFileContentsRaw()));
						} else if (!"text".equals(mediaType.getType()) && !ADDITIONAL_TYPES.contains(mediaType)) {
							model.addAttribute("rawError", "Cannot display file.");
						}

						model.addAttribute("file", repoFile);

						if (specialMarkupService.isSpecialMarkup(repoFile.getName())) {
							model.addAttribute(SPECIAL_MARKUP, specialMarkupService.retrieveMarkup(repo, db, repoFile, ref));
						}

						return "git/file";
					}
				} else {
					if (repoUtils.isPathRoot(path)) {
						model.addAttribute(SPECIAL_MARKUP, specialMarkupService.resolveReadMeFile(repo, db, ref, files));
					} else {
						addReadmeIfExists(repo, ref, model, db, files);
					}

					model.addAttribute("files", files);
				}
			}
			return "git/tree";
		});
	}

	private void addReadmeIfExists(final GitRepository repo, final String ref, final Model model, final Repository db, final List<RepoFile> files) throws GitServerErrorException {
		for (final RepoFile aFile : files) {
			if (!aFile.isDirectory() && specialMarkupService.isReadmeFile(aFile.getName())) {
				model.addAttribute(SPECIAL_MARKUP,
						new ReadmeFile(aFile.getName(), specialMarkupService.retrieveMarkup(repo, db, aFile, ref)));
				break;
			}
		}
	}

}