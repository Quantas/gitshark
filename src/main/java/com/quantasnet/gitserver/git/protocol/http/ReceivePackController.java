package com.quantasnet.gitserver.git.protocol.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.UnpackException;
import org.eclipse.jgit.http.server.ClientVersionUtil;
import org.eclipse.jgit.http.server.GitSmartHttpTools;
import org.eclipse.jgit.http.server.ServletUtils;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.InternalHttpServerGlue;
import org.eclipse.jgit.transport.PacketLineOut;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.RefAdvertiser.PacketLineOutRefAdvertiser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.exception.GitSharkErrorException;
import com.quantasnet.gitserver.git.exception.GitSharkException;
import com.quantasnet.gitserver.git.protocol.http.vendor.SmartOutputStream;
import com.quantasnet.gitserver.git.protocol.packs.GitSharkReceivePackFactory;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.user.User;

@RequestMapping("/repo/{repoOwner}/{repoName}.git")
@Controller
public class ReceivePackController {

	private static final Logger LOG = LoggerFactory.getLogger(ReceivePackController.class);

	@Autowired
	private GitSharkReceivePackFactory receivePackFactory;

	@RequestMapping(value = "/info/refs", params = "service=" + Constants.GIT_RECEIVE_PACK, method = RequestMethod.GET, 
			produces = Constants.GIT_RECEIVE_PACK_ADV)
	public ResponseEntity<byte[]> receivePackAdv(final GitRepository repo, @AuthenticationPrincipal final User user, @RequestHeader(Constants.HEADER_USER_AGENT) String userAgent) throws GitSharkException {
		final ByteArrayOutputStream buf = new ByteArrayOutputStream();
		
		repo.execute(db -> {
			final ReceivePack rp = receivePackFactory.createReceivePack(db, repo, user);
			rp.setRefLogIdent(new PersonIdent(user.getUsername(), user.getEmail()));
			InternalHttpServerGlue.setPeerUserAgent(rp, userAgent);
			
			final PacketLineOut packetOut = new PacketLineOut(buf);
			packetOut.writeString("# service=" + Constants.GIT_RECEIVE_PACK + "\n");
			packetOut.end();
			
			try {
				rp.sendAdvertisedRefs(new PacketLineOutRefAdvertiser(packetOut));
			} finally {
				rp.getRevWalk().close();
			}
		});
		
		return new ResponseEntity<>(buf.toByteArray(), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/" + Constants.GIT_RECEIVE_PACK, method = RequestMethod.POST, 
			consumes = Constants.GIT_RECEIVE_PACK_REQUEST, 
			produces = Constants.GIT_RECEIVE_PACK_RESULT)
	public void receivePack(final GitRepository repo, @AuthenticationPrincipal final User user, @RequestHeader(Constants.HEADER_USER_AGENT) String userAgent, final HttpServletRequest req, final HttpServletResponse rsp) throws GitSharkException {
		
		final int[] version = ClientVersionUtil.parseVersion(userAgent);
		
		if (ClientVersionUtil.hasChunkedEncodingRequestBug(version, req)) {
			try {
				rsp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			} catch (final IOException e) {
				throw new GitSharkErrorException(e);
			}
			return;
		}
		
		final SmartOutputStream out = new SmartOutputStream(req, rsp, true);
		
		repo.execute(db -> {
			final ReceivePack rp = receivePackFactory.createReceivePack(db, repo, user);
			try {
				rp.setBiDirectionalPipe(false);
				rp.setEchoCommandFailures(ClientVersionUtil.hasPushStatusBug(version));
				rsp.setContentType(Constants.GIT_RECEIVE_PACK_RESULT);
				rp.receive(ServletUtils.getInputStream(req), out, null);
				out.close();
			} catch (final CorruptObjectException | UnpackException e) {
				LOG.error("Error receiving push", e);
				ServletUtils.consumeRequestBody(req);
				out.close();
			} catch (final Throwable e) {
				LOG.error("Really bad error receiving push", e);
				if (!rsp.isCommitted()) {
					rsp.reset();
					GitSmartHttpTools.sendError(req, rsp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			}
		});
	}
}