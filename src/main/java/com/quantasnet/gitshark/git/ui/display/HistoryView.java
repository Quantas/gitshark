package com.quantasnet.gitshark.git.ui.display;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.Repository;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.quantasnet.gitshark.git.exception.GitSharkErrorException;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.model.Commit;
import com.quantasnet.gitshark.git.model.RepoFile;
import com.quantasnet.gitshark.git.repo.GitRepository;

@Component
class HistoryView implements DisplayView {

	@Override
	public Object display(final GitRepository repo, final String ref, final Model model, final String path, final Repository db, final List<RepoFile> files) throws GitSharkException {
		try {
			final LogCommand logCommand = Git.wrap(db).log().setMaxCount(50);

			if (path.length() > 1) {
				logCommand.addPath(path);
			}

			final List<Commit> history = StreamSupport.stream(logCommand.call().spliterator(), false)
					.map(historyCommit -> new Commit(historyCommit, repo))
					.collect(Collectors.toList());

			model.addAttribute("historyPos", ref);
			model.addAttribute("history", history);
			return "git/history";
		} catch (Exception e) {
			throw new GitSharkErrorException(e);
		}
	}

	@Override
	public DisplayType getType() {
		return DisplayType.HISTORY;
	}
}
