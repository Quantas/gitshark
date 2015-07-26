package com.quantasnet.gitserver.git.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Asciidoctor.Factory;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
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
import com.quantasnet.gitserver.git.model.ReadmeFile;
import com.quantasnet.gitserver.git.model.RepoFile;
import com.quantasnet.gitserver.git.repo.GitRepository;

@Component
public class ReadmeFileService {

	private static final Logger LOG = LoggerFactory.getLogger(ReadmeFileService.class);

	private static final List<String> ASCIIDOC_NAMES = Arrays.asList("readme.adoc", "readme.ad");

	private static final List<String> MARKDOWN_NAMES = Arrays.asList("readme.md", "readme.markdown");

	private final Asciidoctor asciidoctor = Factory.create();

	@Autowired
	private RepositoryUtilities repoUtils;
	
	@Cacheable(cacheNames = RepoCacheService.ALL_READMES, key = "{ #repo.fullDisplayName, #branch }")
	public ReadmeFile resolveReadMeFile(final GitRepository repo, final Repository db, final String branch) throws GitServerException {
		return resolveReadMeFile(repo, db, branch, repoUtils.getFiles(repo, db, branch, "", false));
	}
	
	@Cacheable(cacheNames = RepoCacheService.ALL_READMES, key = "{ #repo.fullDisplayName, #branch }")
	public ReadmeFile resolveReadMeFile(final GitRepository repo, final Repository db, final String branch, final List<RepoFile> files) throws GitServerException {
		LOG.info("Cache Miss - Readme File - {} - {}", repo.getFullDisplayName(), branch);
		try {
			if (!files.isEmpty()) {
				for (final RepoFile file : files) {
					final String fileName = file.getName();
					if (ASCIIDOC_NAMES.contains(fileName.toLowerCase())) {
						final String readme = new String(repoUtils.getFileContents(db, ObjectId.fromString(file.getObjectId())));
						return new ReadmeFile(fileName, renderAsciiDoc(readme));
					} else if (MARKDOWN_NAMES.contains(fileName.toLowerCase())) {
						final String readme = new String(repoUtils.getFileContents(db, ObjectId.fromString(file.getObjectId())));
						return new ReadmeFile(fileName, renderMarkdown(readme));
					}
				}
			}
			return null;
		} catch (final IOException e) {
			throw new GitServerErrorException(e);
		}
	}
	
	private String renderAsciiDoc(final String originalText) {
		final Options options = OptionsBuilder
			.options()
			.safe(SafeMode.SERVER)
			.backend("xhtml5")
			.attributes(AttributesBuilder.attributes().showTitle(true))
			.get();
		return asciidoctor.render(originalText, options);
	}

	private String renderMarkdown(final String originalText) {
		return new PegDownProcessor().markdownToHtml(originalText);
	}
}
