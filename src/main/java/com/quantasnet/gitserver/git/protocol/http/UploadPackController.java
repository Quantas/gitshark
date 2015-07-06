package com.quantasnet.gitserver.git.protocol.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.http.server.ClientVersionUtil;
import org.eclipse.jgit.http.server.GitSmartHttpTools;
import org.eclipse.jgit.http.server.ServletUtils;
import org.eclipse.jgit.transport.InternalHttpServerGlue;
import org.eclipse.jgit.transport.PacketLineOut;
import org.eclipse.jgit.transport.RefAdvertiser.PacketLineOutRefAdvertiser;
import org.eclipse.jgit.transport.ServiceMayNotContinueException;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.UploadPackInternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.protocol.http.vendor.SmartOutputStream;
import com.quantasnet.gitserver.git.repo.GitRepository;

@RequestMapping("/repo/{repoOwner}/{repoName}.git")
@Controller
public class UploadPackController {

	private static final Logger LOG = LoggerFactory.getLogger(UploadPackController.class);
	
	@RequestMapping(value = "/info/refs", params = "service=" + Constants.GIT_UPLOAD_PACK, method = RequestMethod.GET, 
			produces = Constants.GIT_UPLOAD_PACK_ADV)
	public ResponseEntity<byte[]> uploadPackAdv(final GitRepository repo, @RequestHeader(Constants.HEADER_USER_AGENT) String userAgent) throws GitServerException {
		final ByteArrayOutputStream buf = new ByteArrayOutputStream();
		
		repo.execute(db -> {
			final UploadPack up = new UploadPack(db);
			InternalHttpServerGlue.setPeerUserAgent(up, userAgent);
			
			final PacketLineOut packetOut = new PacketLineOut(buf);
			packetOut.writeString("# service=" + Constants.GIT_UPLOAD_PACK + "\n");
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
	
	@RequestMapping(value = "/" + Constants.GIT_UPLOAD_PACK, method = RequestMethod.POST, 
			consumes = Constants.GIT_UPLOAD_PACK_REQUEST, 
			produces = Constants.GIT_UPLOAD_PACK_RESULT)
	public void uploadPack(final GitRepository repo, @RequestHeader(Constants.HEADER_USER_AGENT) String userAgent, final HttpServletRequest req, final HttpServletResponse rsp) throws GitServerException, IOException {
		
		final int[] version = ClientVersionUtil.parseVersion(userAgent);
		if (ClientVersionUtil.hasChunkedEncodingRequestBug(version, req)) {
			rsp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			return;
		}
		
		final SmartOutputStream out = new SmartOutputStream(req, rsp, true);
		
		repo.execute(db -> {
			UploadPack up = new UploadPack(db);
			try {
				up.setBiDirectionalPipe(false);
				rsp.setContentType(Constants.GIT_UPLOAD_PACK_RESULT);

				up.upload(ServletUtils.getInputStream(req), out, null);
				out.close();

			} catch (final ServiceMayNotContinueException e) {
				LOG.error("Service May Not Continue", e);
				if (e.isOutput()) {
					ServletUtils.consumeRequestBody(req);
					out.close();
				} else if (!rsp.isCommitted()) {
					rsp.reset();
					GitSmartHttpTools.sendError(req, rsp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
				}
				return;
			} catch (final UploadPackInternalServerErrorException e) {
				ServletUtils.consumeRequestBody(req);
				IOUtils.closeQuietly(out);
				LOG.error("UploadPack Error Exception", e);
			} catch (final Throwable e) {
				LOG.error("Horrible exception while receiving UploadPack", e);
				if (!rsp.isCommitted()) {
					rsp.reset();
					GitSmartHttpTools.sendError(req, rsp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
				return;
			}
		});
	}
}