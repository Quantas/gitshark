package com.quantasnet.gitshark.git.protocol.git;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.transport.PacketLineIn;
import org.eclipse.jgit.transport.UploadPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.quantasnet.gitshark.Constants;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.exception.RepositoryAccessDeniedException;
import com.quantasnet.gitshark.git.protocol.packs.GitSharkReceivePackFactory;
import com.quantasnet.gitshark.git.repo.GitRepository;

@Scope("prototype")
@Component
public class GitProtocolClientThread extends Thread {

	private static final Logger LOG = LoggerFactory.getLogger(GitProtocolClientThread.class);
	
	@Autowired
	private GitSharkReceivePackFactory receivePackFactory;
	
	private Socket socket;
	
	public GitProtocolClientThread setup(final Socket socket) {
		setName("GitProtocolClient-" + socket.getRemoteSocketAddress().toString());
		this.socket = socket;
		return this;
	}
	
	@Override
	public void run() {
		if (null == socket) {
			throw new IllegalStateException("Socket must be populated, was setup(Socket) called??");
		}
		
		try {
			LOG.info("Accepted connection from {}", socket.getRemoteSocketAddress());
			
			final InputStream input = new BufferedInputStream(socket.getInputStream());
			final OutputStream output = new BufferedOutputStream(socket.getOutputStream());
			
			String command = new PacketLineIn(input).readStringRaw();
			
			final int nullByte = command.indexOf('\0');
			if (nullByte > -1) {
				command = command.substring(0, nullByte);
			}
			command = command.replace('\\', '/');
			
			final String[] commands = command.split("\\s+");
			final String[] repoPath = commands[1].split("/");
			
			final String requestedMethod = commands[0];
			final String owner = repoPath[1];
			final String repo = repoPath[2];
			
			handleRequest(input, output, requestedMethod, owner, repo);
		} catch(final IOException e) {
			LOG.error("Error in client thread", e);
		}
	}

	private void handleRequest(final InputStream input, final OutputStream output, final String requestedMethod, final String owner, final String repo) {
		try {
			final GitRepository gitRepo = null; // TODO
			
			if (Constants.GIT_UPLOAD_PACK.equals(requestedMethod)) {
				if (gitRepo.isAnonRead()) {
					gitRepo.execute(db -> new UploadPack(db).upload(input, output, null));
				} else {
					throw new RepositoryAccessDeniedException();
				}
			} else if (Constants.GIT_RECEIVE_PACK.equals(requestedMethod)) {
				if (gitRepo.isAnonWrite()) {
					gitRepo.execute(db -> receivePackFactory.createReceivePack(db, gitRepo, null).receive(input, output, null));
				} else {
					throw new RepositoryAccessDeniedException();
				}
			}
		} catch (final RepositoryAccessDeniedException | GitSharkException e) {
			LOG.error("Probably failed...", e);
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(output);
		}
	}
	
}
