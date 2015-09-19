package com.quantasnet.gitshark.git.ui;

import java.util.stream.Collectors;

import org.eclipse.jgit.internal.storage.dfs.DfsObjDatabase;
import org.eclipse.jgit.internal.storage.dfs.DfsPackDescription;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.pack.PackExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.quantasnet.gitshark.Constants;
import com.quantasnet.gitshark.git.cache.EvictAllCaches;
import com.quantasnet.gitshark.git.cache.EvictRepoCache;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsService;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.repo.GitRepository;
import com.quantasnet.gitshark.user.User;

@RequestMapping("/repo/{repoOwner}/{repoName}/settings")
@Controller
public class SettingsController {

	@Autowired
	private GitSharkDfsService dfsService;

	@RequestMapping(method = RequestMethod.GET)
	public String settings(final GitRepository repo, final Model model) throws GitSharkException {
		if (repo.hasCommits()) {
			model.addAttribute("files", dfsService.getPacks(repo.getId(), new DfsRepositoryDescription(repo.getName()))
					.stream()
					.map(this::buildModel)
					.collect(Collectors.toList()));
		}
		return "git/settings";
	}

	@EvictAllCaches
	@EvictRepoCache
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public String deleteRepository(final GitRepository repo, @AuthenticationPrincipal final User user, final RedirectAttributes redirectAttributes) {
		if (dfsService.deleteRepo(repo.getName(), user)) {
			redirectAttributes.addFlashAttribute(Constants.SUCCESS_STATUS, "Repository " + repo.getFullDisplayName() + " was successfully deleted.");
		} else {
			redirectAttributes.addFlashAttribute(Constants.FAILURE_STATUS, "Repository " + repo.getFullDisplayName() + " could not be deleted.");
		}
		
		return "redirect:/repo";
	}

	private PackModel buildModel(final DfsPackDescription pack) {
		final PackExt ext;
		if (pack.hasFileExt(PackExt.INDEX)) {
			ext = PackExt.INDEX;
		} else if (pack.hasFileExt(PackExt.PACK)) {
			ext = PackExt.PACK;
		} else {
			ext = PackExt.BITMAP_INDEX;
		}

		return new PackModel(pack.getFileName(ext), ext, pack.getPackSource());
	}

	private class PackModel {
		private final String fileName;
		private final PackExt extension;
		private final DfsObjDatabase.PackSource packSource;

		public PackModel(String fileName, PackExt extension, DfsObjDatabase.PackSource packSource) {
			this.fileName = fileName;
			this.extension = extension;
			this.packSource = packSource;
		}

		public String getFileName() {
			return fileName;
		}

		public PackExt getExtension() {
			return extension;
		}

		public DfsObjDatabase.PackSource getPackSource() {
			return packSource;
		}
	}
}