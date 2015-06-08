package com.quantasnet.gitserver.git.http;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.eclipse.jgit.http.server.ClientVersionUtil.hasChunkedEncodingRequestBug;
import static org.eclipse.jgit.http.server.ClientVersionUtil.hasPushStatusBug;
import static org.eclipse.jgit.http.server.ClientVersionUtil.parseVersion;
import static org.eclipse.jgit.http.server.GitSmartHttpTools.RECEIVE_PACK_RESULT_TYPE;
import static org.eclipse.jgit.http.server.GitSmartHttpTools.sendError;
import static org.eclipse.jgit.http.server.ServletUtils.consumeRequestBody;
import static org.eclipse.jgit.http.server.ServletUtils.getInputStream;

import java.io.ByteArrayOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.UnpackException;
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

import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.jgit.vendor.SmartOutputStream;

@RequestMapping("/repo/{repoOwner}/{repoName}.git")
@Controller
public class ReceivePackController {

	@RequestMapping(value = "/info/refs", params = "service=git-receive-pack", method = RequestMethod.GET, produces = "application/x-git-receive-pack-advertisement")
	public ResponseEntity<byte[]> receivePackAdv(final GitRepository repo, @AuthenticationPrincipal final User user, final HttpServletRequest req, @RequestHeader(value="User-Agent") String userAgent) throws Exception {
		final ByteArrayOutputStream buf = new ByteArrayOutputStream();
		
		GitRepository.execute(repo, db -> {
			final ReceivePack rp = new ReceivePack(db);
			rp.setRefLogIdent(new PersonIdent(user.getUsername(), user.getUsername() + "@" + req.getRemoteHost()));
			InternalHttpServerGlue.setPeerUserAgent(rp, userAgent);
			
			final PacketLineOut packetOut = new PacketLineOut(buf);
			packetOut.writeString("# service=git-receive-pack\n");
			packetOut.end();
			
			try {
				rp.sendAdvertisedRefs(new PacketLineOutRefAdvertiser(packetOut));
			} finally {
				rp.getRevWalk().close();
			}
		});
		
		return new ResponseEntity<byte[]>(buf.toByteArray(), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/git-receive-pack", method = RequestMethod.POST, 
			consumes = "application/x-git-receive-pack-request", 
			produces = "application/x-git-receive-pack-result")
	public void receivePack(final GitRepository repo, @RequestHeader(value="User-Agent") String userAgent, final HttpServletRequest req, final HttpServletResponse rsp) throws Exception {
		
		int[] version = parseVersion(userAgent);
		if (hasChunkedEncodingRequestBug(version, req)) {
			rsp.sendError(SC_UNSUPPORTED_MEDIA_TYPE);
			return;
		}
		
		final SmartOutputStream out = new SmartOutputStream(req, rsp, true);
		
		GitRepository.execute(repo, db -> {
			final ReceivePack rp = new ReceivePack(db);
			try {
				rp.setBiDirectionalPipe(false);
				rp.setEchoCommandFailures(hasPushStatusBug(version));
				rsp.setContentType(RECEIVE_PACK_RESULT_TYPE);
				rp.receive(getInputStream(req), out, null);
				out.close();
			} catch (CorruptObjectException | UnpackException e) {
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