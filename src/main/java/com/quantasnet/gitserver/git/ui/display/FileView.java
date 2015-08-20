package com.quantasnet.gitserver.git.ui.display;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import org.apache.tika.Tika;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.google.common.collect.ImmutableSet;
import com.quantasnet.gitserver.git.exception.GitSharkErrorException;
import com.quantasnet.gitserver.git.exception.GitSharkException;
import com.quantasnet.gitserver.git.model.RepoFile;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.git.service.SpecialMarkupService;

@Component
class FileView implements DisplayView {

	private static final String SPECIAL_MARKUP = "specialmarkup";

	private static final Set<MediaType> ADDITIONAL_TYPES = new ImmutableSet.Builder<MediaType>()
			.add(MediaType.APPLICATION_XML)
			.add(MediaType.APPLICATION_JSON)
			.add(MediaType.APPLICATION_XHTML_XML)
			.build();

	private static final Tika TIKA = new Tika();

	@Autowired
	private SpecialMarkupService specialMarkupService;

	@Override
	public Object display(final GitRepository repo, final String ref, final Model model, final String path, final Repository db, final List<RepoFile> files) throws GitSharkException {
		final RepoFile repoFile = files.get(0);

		if (null != repoFile) {
			try {
				final String mediaTypeString = TIKA.detect(new ByteArrayInputStream(repoFile.getFileContentsRaw()));
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
			} catch (final IOException ioe) {
				throw new GitSharkErrorException(ioe);
			}

			return "git/file";
		}

		throw new GitSharkErrorException("repoFile was null");
	}

	@Override
	public DisplayType getType() {
		return DisplayType.FILE;
	}
}
