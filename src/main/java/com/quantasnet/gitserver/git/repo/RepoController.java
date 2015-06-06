package com.quantasnet.gitserver.git.repo;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.eclipse.jgit.http.server.ClientVersionUtil.hasChunkedEncodingRequestBug;
import static org.eclipse.jgit.http.server.ClientVersionUtil.hasPushStatusBug;
import static org.eclipse.jgit.http.server.ClientVersionUtil.parseVersion;
import static org.eclipse.jgit.http.server.GitSmartHttpTools.RECEIVE_PACK_RESULT_TYPE;
import static org.eclipse.jgit.http.server.GitSmartHttpTools.UPLOAD_PACK_RESULT_TYPE;
import static org.eclipse.jgit.http.server.GitSmartHttpTools.sendError;
import static org.eclipse.jgit.http.server.ServletUtils.consumeRequestBody;
import static org.eclipse.jgit.http.server.ServletUtils.getInputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.UnpackException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.transport.InternalHttpServerGlue;
import org.eclipse.jgit.transport.PacketLineOut;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.RefAdvertiser;
import org.eclipse.jgit.transport.RefAdvertiser.PacketLineOutRefAdvertiser;
import org.eclipse.jgit.transport.ServiceMayNotContinueException;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.UploadPackInternalServerErrorException;
import org.eclipse.jgit.util.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.jgit.vendor.SmartOutputStream;


@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class RepoController {

	private static final Logger LOG = LoggerFactory.getLogger(RepoController.class);
	
	@RequestMapping(value = "/" + Constants.HEAD, method = RequestMethod.GET)
	public ResponseEntity<byte[]> head(final GitRepository repo) throws IOException {
		final byte[] head = IO.readFully(new File(repo.getFullRepoDirectory(), Constants.HEAD));
		return new ResponseEntity<byte[]>(head, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/info/refs", method = RequestMethod.GET, produces = "text/plain")
	public ResponseEntity<String> infoRefs(final GitRepository repo) throws Exception {
		final StringBuilder output = new StringBuilder();
		
		GitRepository.execute(repo, db -> {
			final RefAdvertiser adv = new RefAdvertiser() {
				@Override
				protected void writeOne(final CharSequence line) throws IOException {
					output.append(line.toString().replace(' ', '\t'));
				}

				@Override
				protected void end() {
				}
			};
			
			adv.init(db);
			adv.setDerefTags(true);

			final Map<String, Ref> refs = db.getRefDatabase().getRefs(RefDatabase.ALL);
			refs.remove(Constants.HEAD);
			adv.send(refs);
		});
		
		return new ResponseEntity<String>(output.toString(), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/info/refs", params = "service=git-receive-pack", method = RequestMethod.GET, produces = "application/x-git-receive-pack-advertisement")
	public ResponseEntity<byte[]> receivePackAdv(final GitRepository repo, @AuthenticationPrincipal final User user, final HttpServletRequest req, @RequestHeader(value="User-Agent") String userAgent) throws ServiceMayNotContinueException, IOException, Exception {
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
	public void receivePack(final GitRepository repo, @AuthenticationPrincipal final User user, @RequestHeader(value="User-Agent") String userAgent, final HttpServletRequest req, final HttpServletResponse rsp) throws Exception {
		
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
	public void uploadPack(final GitRepository repo, @AuthenticationPrincipal final User user, @RequestHeader(value="User-Agent") String userAgent, final HttpServletRequest req, final HttpServletResponse rsp) throws Exception {
		
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
		
	
	@RequestMapping({ "", "/", "/**" })
	public ResponseEntity<Object> repo(final GitRepository repo, final HttpServletRequest req) {
		LOG.info("Path requested ={}, {}", req.getMethod(), req.getServletPath());
		LOG.info("Parameters = {}", req.getParameterMap());
		return new ResponseEntity<Object>("Fail", HttpStatus.OK);
	}
	
}
