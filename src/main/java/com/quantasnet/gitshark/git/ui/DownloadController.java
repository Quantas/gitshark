package com.quantasnet.gitshark.git.ui;

import java.io.ByteArrayOutputStream;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.eclipse.jgit.api.ArchiveCommand;
import org.eclipse.jgit.api.ArchiveCommand.Format;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.archive.Tbz2Format;
import org.eclipse.jgit.archive.TgzFormat;
import org.eclipse.jgit.archive.ZipFormat;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.quantasnet.gitshark.git.exception.GitSharkErrorException;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.repo.GitRepository;

@RequestMapping("/repo/{repoOwner}/{repoName}/download")
@Controller
public class DownloadController {
	
	@RequestMapping(value = "/{branch}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> downloadBranch(final GitRepository repo, @PathVariable final String branch, @RequestParam(required = false, defaultValue = "zip") final String format) throws GitSharkException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		final Formats formats = Formats.getForExtension(format);
		
		final StringBuilder commitId = new StringBuilder();
		
		repo.execute(db -> {
			try {
				RevCommit commit = null;
				try (final RevWalk revWalk = new RevWalk(db)) {
					commit = revWalk.parseCommit(db.getRef(branch).getObjectId());
					commitId.append(commit.getId().getName().substring(0, 7));
				}
	
				ArchiveCommand.registerFormat(formats.extension, formats.newInstance());
				
				Git.wrap(db).archive()
			    .setTree(db.resolve(branch))
			    .setFormat(format)
			    .setOutputStream(out)
			    .call();
				
				ArchiveCommand.unregisterFormat(formats.extension);
			} catch (final GitAPIException | ReflectiveOperationException e) {
				throw new GitSharkErrorException(e);
			}
		});
		
		final byte[] zipfile = out.toByteArray();
		
		final HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
	    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + buildFileName(repo, commitId.toString(), format));
	    headers.setContentLength(zipfile.length);

	    return new ResponseEntity<>(zipfile, headers, HttpStatus.OK);
	}
	
	private String buildFileName(final GitRepository repo, final String commitId, final String format) {
		return repo.getOwner() + '-' + repo.getDisplayName() + '-' + commitId + '.' + format;
	}
	
	private enum Formats {
		ZIP("zip", ZipFormat.class),
		TARGZ("tar.gz", TgzFormat.class),
		TARBZ2("tar.bz2", Tbz2Format.class);

		private static final Formats[] VALUES = values();
		
		private String extension;
		private Class<?> format;
		
		Formats(final String extension, final Class<?> format) {
			this.extension = extension;
			this.format = format;
		}
		
		public static Formats getForExtension(final String ext) {
			for (final Formats format : VALUES) {
				if (format.extension.equals(ext)) {
					return format;
				}
			}
			
			return null;
		}
		
		@SuppressWarnings("unchecked")
		public Format<ArchiveOutputStream> newInstance() throws ReflectiveOperationException {
			return (Format<ArchiveOutputStream>) format.newInstance();
		}
	}
}
