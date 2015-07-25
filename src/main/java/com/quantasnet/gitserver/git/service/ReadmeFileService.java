package com.quantasnet.gitserver.git.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Asciidoctor.Factory;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.git.exception.GitServerErrorException;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.model.RepoFile;
import com.quantasnet.gitserver.git.repo.GitRepository;

@Component
public class ReadmeFileService {

	private static final Logger LOG = LoggerFactory.getLogger(ReadmeFileService.class);

	private final Asciidoctor asciidoctor = Factory.create();

	@Autowired
	private RepositoryUtilities repoUtils;
	
	@Cacheable(cacheNames = RepoCacheService.ALL_READMES, key = "{ #repo.fullDisplayName, #branch }")
	public String resolveReadMeFile(final GitRepository repo, final Repository db, final String branch) throws GitServerException {
		LOG.info("Cache Miss - Readme File - {} - {}", repo.getFullDisplayName(), branch);
		return resolveReadMeFile(repo, db, repoUtils.getFiles(repo, db, branch, "", false));
	}
	
	
	@Cacheable(cacheNames = RepoCacheService.README, key = "#repo.fullDisplayName")
	public String resolveReadMeFile(final GitRepository repo, final Repository db, final List<RepoFile> files) throws GitServerException {
		LOG.info("Cache Miss - Readme File - {}", repo.getFullDisplayName());
		try {
			if (!files.isEmpty()) {
				for (final RepoFile file : files) {
					final String fileName = file.getName();
					if ("readme.md".equalsIgnoreCase(fileName) || "readme.markdown".equalsIgnoreCase(fileName)) {
						final String markdown = new String(repoUtils.getFileContents(db, ObjectId.fromString(file.getObjectId())));
						return renderMarkdown(markdown);
					} else if ("readme.adoc".equalsIgnoreCase(fileName)) {
						final String adoc = new String(repoUtils.getFileContents(db, ObjectId.fromString(file.getObjectId())));
						return renderAsciiDoc(adoc);
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
	
	private String renderAsciiDoc(final String originalText) {
		return asciidoctor.convert(originalText, new HashMap<>());
	}
}
