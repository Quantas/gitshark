package com.quantasnet.gitserver.git.http;

import java.io.ByteArrayOutputStream;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.jgit.vendor.SmartOutputStream;

@RequestMapping("/repo/{repoOwner}/{repoName}.git")
@Controller
public class ReceivePackController {

	@RequestMapping(value = "/info/refs", params = "service=" + Constants.GIT_RECEIVE_PACK, method = RequestMethod.GET, 
			produces = Constants.GIT_RECEIVE_PACK_ADV)
	public ResponseEntity<byte[]> receivePackAdv(final GitRepository repo, @AuthenticationPrincipal final User user, final HttpServletRequest req, @RequestHeader(Constants.HEADER_USER_AGENT) String userAgent) throws Exception {
		final ByteArrayOutputStream buf = new ByteArrayOutputStream();
		
		GitRepository.execute(repo, db -> {
			final ReceivePack rp = new ReceivePack(db);
			// TODO replace email with user's email
			rp.setRefLogIdent(new PersonIdent(user.getUsername(), user.getUsername() + "@" + req.getRemoteHost()));
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
		
		return new ResponseEntity<byte[]>(buf.toByteArray(), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/" + Constants.GIT_RECEIVE_PACK, method = RequestMethod.POST, 
			consumes = Constants.GIT_RECEIVE_PACK_REQUEST, 
			produces = Constants.GIT_RECEIVE_PACK_RESULT)
	public void receivePack(final GitRepository repo, @RequestHeader(Constants.HEADER_USER_AGENT) String userAgent, final HttpServletRequest req, final HttpServletResponse rsp) throws Exception {
		
		final int[] version = ClientVersionUtil.parseVersion(userAgent);
		
		if (ClientVersionUtil.hasChunkedEncodingRequestBug(version, req)) {
			rsp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			return;
		}
		
		final SmartOutputStream out = new SmartOutputStream(req, rsp, true);
		
		GitRepository.execute(repo, db -> {
			final ReceivePack rp = new ReceivePack(db);
			try {
				rp.setBiDirectionalPipe(false);
				rp.setEchoCommandFailures(ClientVersionUtil.hasPushStatusBug(version));
				rsp.setContentType(Constants.GIT_RECEIVE_PACK_RESULT);
				rp.receive(ServletUtils.getInputStream(req), out, null);
				out.close();
			} catch (final CorruptObjectException | UnpackException e) {
				ServletUtils.consumeRequestBody(req);
				out.close();
			} catch (final Throwable e) {
				if (!rsp.isCommitted()) {
					rsp.reset();
					GitSmartHttpTools.sendError(req, rsp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
				return;
			}
		});
	}
}