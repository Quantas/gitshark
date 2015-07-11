package com.quantasnet.gitserver.git.protocol.git;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;

import com.quantasnet.gitserver.git.protocol.packs.GitServerReceivePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.quantasnet.gitserver.git.repo.FilesystemRepositoryService;

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
	private FilesystemRepositoryService repositoryService;

	@Autowired
	private GitServerReceivePackFactory receivePackFactory;
	
	private boolean isRunning = true;
	
	private ServerSocket serverSocket;
	
	private Thread serviceThread;
	
	public void start() throws IOException {
		serverSocket = new ServerSocket(GIT_PORT, BACKLOG);
		
		serviceThread = new Thread("GitProtocolService-Accept") {
			@Override
			public void run() {
				LOG.info("GitProtocolService is running on {}", serverSocket.getLocalSocketAddress());
				
				while(isRunning) {
					try {
						new GitProtocolClientThread(serverSocket.accept(), repositoryService, receivePackFactory).start();
					} catch (final InterruptedIOException e) {
						LOG.trace("InterruptedIOException while waiting for clients", e);
					} catch (final IOException e) {
						LOG.trace("Excpected exception in Socket accept", e);
						LOG.info("Stopping GitProtocolService...");
						break;
					}
				}
			}
		};
		
		serviceThread.start();
	}
	
	public void stop() {
		isRunning = false;
		
		try {
			serverSocket.close();
			serviceThread.join();
			LOG.info("GitProtocolService stopped");
		} catch (final Exception e) {
			LOG.error("Error stopping GitProtocolService", e);
		}
	}
}
