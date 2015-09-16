package com.quantasnet.gitshark.git.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
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
import org.springframework.stereotype.Service;

import com.quantasnet.gitshark.git.cache.RepoCacheConstants;
import com.quantasnet.gitshark.git.exception.GitSharkErrorException;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.model.ReadmeFile;
import com.quantasnet.gitshark.git.model.RepoFile;
import com.quantasnet.gitshark.git.repo.GitRepository;

@Service
public class SpecialMarkupService {

	private static final Logger LOG = LoggerFactory.getLogger(SpecialMarkupService.class);

	private static final List<String> ADOC_POSTFIXES = Arrays.asList("adoc", "ad");
	private static final List<String> ASCIIDOC_NAMES = Arrays.asList("readme.adoc", "readme.ad");

	private static final List<String> MD_POSTFIXES = Arrays.asList("md", "markdown");
	private static final List<String> MARKDOWN_NAMES = Arrays.asList("readme.md", "readme.markdown");

	private Asciidoctor asciidoctor;

	@Autowired
	private RepositoryUtilities repoUtils;

	@Autowired
	private ServletContext servletContext;

	public SpecialMarkupService() {
		try {
			asciidoctor = Factory.create();
		} catch (final Throwable t) {
			LOG.error("Error loading AsciiDoctor!", t);
		}
	}

	public boolean isSpecialMarkup(final String fileName) {
		final String postfix = getFilePostfix(fileName);
		return ADOC_POSTFIXES.contains(postfix) || MD_POSTFIXES.contains(postfix);
	}

	public boolean isReadmeFile(final String fileName) {
		final String fileNameLower = fileName.toLowerCase();
		return ASCIIDOC_NAMES.contains(fileNameLower) || MARKDOWN_NAMES.contains(fileNameLower);
	}

	public String retrieveMarkup(final GitRepository repo, final Repository db, final RepoFile file, final String branch) throws GitSharkErrorException {
		try {
			final String postfix = getFilePostfix(file.getName());
			final String content = file.getFileContents() == null ? new String(repoUtils.getFileContents(db, ObjectId.fromString(file.getObjectId()))) : file.getFileContents();
			if (ADOC_POSTFIXES.contains(postfix)) {
				return renderAsciiDoc(content, repo, branch, file.getParent());
			} else if (MD_POSTFIXES.contains(postfix)) {
				return renderMarkdown(content);
			}
		} catch (final IOException e) {
			throw new GitSharkErrorException(e);
		}

		return null;
	}

	@Cacheable(cacheNames = RepoCacheConstants.ALL_READMES, key = "{ #repo.fullDisplayName, #branch }")
	public ReadmeFile resolveReadMeFile(final GitRepository repo, final Repository db, final String branch) throws GitSharkException {
		return resolveReadMeFile(repo, db, branch, repoUtils.getFiles(repo, db, branch, ""));
	}
	
	@Cacheable(cacheNames = RepoCacheConstants.ALL_READMES, key = "{ #repo.fullDisplayName, #branch }")
	public ReadmeFile resolveReadMeFile(final GitRepository repo, final Repository db, final String branch, final List<RepoFile> files) throws GitSharkException {
		LOG.info("Cache Miss - Readme File - {} - {}", repo.getFullDisplayName(), branch);
		try {
			if (!files.isEmpty()) {
				for (final RepoFile file : files) {
					final String fileName = file.getName();
					if (ASCIIDOC_NAMES.contains(fileName.toLowerCase())) {
						final String readme = new String(repoUtils.getFileContents(db, ObjectId.fromString(file.getObjectId())));
						return new ReadmeFile(fileName, renderAsciiDoc(readme, repo, branch, ""));
					} else if (MARKDOWN_NAMES.contains(fileName.toLowerCase())) {
						final String readme = new String(repoUtils.getFileContents(db, ObjectId.fromString(file.getObjectId())));
						return new ReadmeFile(fileName, renderMarkdown(readme));
					}
				}
			}
			return null;
		} catch (final IOException e) {
			throw new GitSharkErrorException(e);
		}
	}

	private String getFilePostfix(final String fileName) {
		return FilenameUtils.getExtension(fileName).toLowerCase();
	}

	private String renderAsciiDoc(final String originalText, final GitRepository repo, final String branch, final String parent) {
		if (null == asciidoctor) {
			return "Error loading AsciiDoctor";
		}

		final Options options = OptionsBuilder
			.options()
			.safe(SafeMode.SERVER)
			.backend("xhtml5")
			.attributes(AttributesBuilder.attributes().showTitle(true))
			.get();
		return asciidoctor.render(fixLinks(originalText, repo, branch, parent), options);
	}

	private String renderMarkdown(final String originalText) {
		return new PegDownProcessor().markdownToHtml(originalText);
	}

	private String fixLinks(final String originalText, final GitRepository repo, final String branch, final String parent) {
		return originalText.replaceAll("(link:|include::)",
				"link:" + servletContext.getContextPath() + "/repo/" + repo.getInterfaceBaseUrl() + "/tree/" + branch + "/" + parent);
	}
}
