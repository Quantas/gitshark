package com.quantasnet.gitserver.git.ui.display;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.quantasnet.gitserver.git.exception.GitServerErrorException;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.model.Commit;
import com.quantasnet.gitserver.git.model.RepoFile;
import com.quantasnet.gitserver.git.repo.GitRepository;

@Component
class HistoryView implements DisplayView {

	@Override
	public Object display(final GitRepository repo, final String ref, final Model model, final String path, final Repository db, final List<RepoFile> files) throws GitServerException {
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
			model.addAttribute("history", history);
			return "git/history";
		} catch (Exception e) {
			throw new GitServerErrorException(e);
		}
	}

	@Override
	public DisplayType getType() {
		return DisplayType.HISTORY;
	}
}
