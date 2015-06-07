package com.quantasnet.gitserver.git.repo.http;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.eclipse.jgit.http.server.ClientVersionUtil.hasChunkedEncodingRequestBug;
import static org.eclipse.jgit.http.server.ClientVersionUtil.parseVersion;
import static org.eclipse.jgit.http.server.GitSmartHttpTools.UPLOAD_PACK_RESULT_TYPE;
import static org.eclipse.jgit.http.server.GitSmartHttpTools.sendError;
import static org.eclipse.jgit.http.server.ServletUtils.consumeRequestBody;
import static org.eclipse.jgit.http.server.ServletUtils.getInputStream;

import java.io.ByteArrayOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.transport.InternalHttpServerGlue;
import org.eclipse.jgit.transport.PacketLineOut;
import org.eclipse.jgit.transport.RefAdvertiser.PacketLineOutRefAdvertiser;
import org.eclipse.jgit.transport.ServiceMayNotContinueException;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.UploadPackInternalServerErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.jgit.vendor.SmartOutputStream;

@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class UploadPackController {

	@RequestMapping(value = "/info/refs", params = "service=git-upload-pack", method = RequestMethod.GET, produces = "application/x-git-upload-pack-advertisement")
	public ResponseEntity<byte[]> uploadPackAdv(final GitRepository repo, @RequestHeader(value="User-Agent") String userAgent) throws Exception {
		final ByteArrayOutputStream buf = new ByteArrayOutputStream();
		
		GitRepository.execute(repo, db -> {
			final UploadPack up = new UploadPack(db);
			InternalHttpServerGlue.setPeerUserAgent(up, userAgent);
			
			final PacketLineOut packetOut = new PacketLineOut(buf);
			packetOut.writeString("# service=git-upload-pack\n");
			packetOut.end();
			
			try {
				up.setBiDirectionalPipe(false);
				up.sendAdvertisedRefs(new PacketLineOutRefAdvertiser(packetOut));
			} finally {
				up.getRevWalk().close();
			}
		});
		
		return new ResponseEntity<byte[]>(buf.toByteArray(), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/git-upload-pack", method = RequestMethod.POST, 
			consumes = "application/x-git-upload-pack-request", 
			produces = "application/x-git-upload-pack-result")
	public void uploadPack(final GitRepository repo, @RequestHeader(value="User-Agent") String userAgent, final HttpServletRequest req, final HttpServletResponse rsp) throws Exception {
		
		int[] version = parseVersion(userAgent);
		if (hasChunkedEncodingRequestBug(version, req)) {
			rsp.sendError(SC_UNSUPPORTED_MEDIA_TYPE);
			return;
		}
		
		final SmartOutputStream out = new SmartOutputStream(req, rsp, true);
		
		GitRepository.execute(repo, db -> {
			UploadPack up = new UploadPack(db);
			try {
				up.setBiDirectionalPipe(false);
				rsp.setContentType(UPLOAD_PACK_RESULT_TYPE);

				up.upload(getInputStream(req), out, null);
				out.close();

			} catch (ServiceMayNotContinueException e) {
				if (e.isOutput()) {
					consumeRequestBody(req);
					out.close();
				} else if (!rsp.isCommitted()) {
					rsp.reset();
					sendError(req, rsp, SC_FORBIDDEN, e.getMessage());
				}
				return;
			} catch (UploadPackInternalServerErrorException e) {
				consumeRequestBody(req);
				out.close();
			} catch (Throwable e) {
				if (!rsp.isCommitted()) {
					rsp.reset();
					sendError(req, rsp, SC_INTERNAL_SERVER_ERROR);
				}
				return;
			}
		});
	}
}