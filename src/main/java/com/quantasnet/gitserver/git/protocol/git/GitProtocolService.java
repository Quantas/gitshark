package com.quantasnet.gitserver.git.protocol.git;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.transport.PacketLineIn;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.UploadPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.git.repo.RepositoryService;

@Component
public class GitProtocolService {

	private static final Logger LOG = LoggerFactory.getLogger(GitProtocolService.class);
	
	/**
	 * Standard port for the git:// protocol
	 */
	private static final int GIT_PORT = 9418;
	
	/**
	 * Max length of waiting queue
	 */
	private static final int BACKLOG = 5;
	
	@Autowired
	private RepositoryService repositoryService;
	
	private boolean isRunning = true;
	
	public void start() throws IOException {
		final ServerSocket serverSocket = new ServerSocket(GIT_PORT, BACKLOG);
		
		new Thread("GitProtocolService-Accept") {
			@Override
			public void run() {
				LOG.info("GitProtocolService is running on {}", serverSocket.getLocalSocketAddress());
				
				while(isRunning) {
					try {
						startNewClient(serverSocket.accept());
					} catch (final IOException e) {
						
					}
				}
				
				try {
					serverSocket.close();
				} catch (final IOException e) {
					// Don't really care about errors here
				}
			};
		}.start();
	}
	
	public void stop() {
		isRunning = false;
	}
	
	private void startNewClient(final Socket socket) {
		new Thread("GitProtocolClient-" + socket.getRemoteSocketAddress().toString()) {
			@Override
			public void run() {
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
					
					try {
						final GitRepository gitRepo = repositoryService.getRepository(null, owner, repo);
						
						if (Constants.GIT_UPLOAD_PACK.equals(requestedMethod)) {
							if (gitRepo.isAnonRead()) {
								GitRepository.execute(gitRepo, db -> {
									new UploadPack(db).upload(input, output, null);
								});
							}
						} else if (Constants.GIT_RECEIVE_PACK.equals(requestedMethod)) {
							if (gitRepo.isAnonWrite()) {
								GitRepository.execute(gitRepo, db -> {
									new ReceivePack(db).receive(input, output, null);
								});
							}
						}
					} catch (final Exception e) {
						LOG.error("Probably failed...", e);
					} finally {
						IOUtils.closeQuietly(input);
						IOUtils.closeQuietly(output);
					}
				} catch(final IOException e) {
					
				}
			}
		}.start();
	}
}