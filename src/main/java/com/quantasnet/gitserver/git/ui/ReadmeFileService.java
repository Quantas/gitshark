package com.quantasnet.gitserver.git.ui;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.pegdown.PegDownProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.git.exception.GitServerErrorException;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.model.RepoFile;
import com.quantasnet.gitserver.git.repo.GitRepository;

@Component
public class ReadmeFileService {

	@Autowired
	private RepositoryUtilities repoUtils;
	
	public String resolveReadMeFile(final GitRepository repo, final Repository db, final String branch) throws GitServerException {
		return resolveReadMeFile(db, repoUtils.getFiles(repo, db, branch, "", false));
	}
	
	public String resolveReadMeFile(final Repository db, final List<RepoFile> files) throws GitServerException {
		try {
			if (!files.isEmpty()) {
				for (final RepoFile file : files) {
					final String fileName = file.getName();
					if (fileName.equalsIgnoreCase("readme.md") || fileName.equalsIgnoreCase("readme.markdown")) {
						final String markdown = new String(repoUtils.getFileContents(db, ObjectId.fromString(file.getObjectId())));
						return renderMarkdown(markdown);
					}
				}
			}
			return null;
		} catch (final IOException e) {
			throw new GitServerErrorException(e);
		}
	}
	
	private String renderMarkdown(final String originalText) {
		final PegDownProcessor pegdown = new PegDownProcessor();
		return pegdown.markdownToHtml(originalText);
	}
}
