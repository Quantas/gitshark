package com.quantasnet.gitshark.git.ui.display;

import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.quantasnet.gitshark.git.exception.GitSharkErrorException;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.model.RepoFile;
import com.quantasnet.gitshark.git.repo.GitRepository;

@Component
class RawView implements DisplayView {

	@Override
	public Object display(final GitRepository repo, final String ref, final Model model, final String path, final Repository db, final List<RepoFile> files) throws GitSharkException {

		final RepoFile repoFile = files.get(0);

		if (null != repoFile) {
			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_PLAIN);
			return new ResponseEntity<>(new String(repoFile.getFileContentsRaw()), headers, HttpStatus.OK);
		}

		throw new GitSharkErrorException("repoFile was null");
	}

	@Override
	public DisplayType getType() {
		return DisplayType.RAW;
	}
}
