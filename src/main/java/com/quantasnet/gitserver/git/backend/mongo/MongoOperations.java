package com.quantasnet.gitserver.git.backend.mongo;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by andrewlandsverk on 8/17/15.
 */
@Component
public class MongoOperations {

	private static final Logger LOG = LoggerFactory.getLogger(MongoOperations.class);

	@Autowired
	private MongoDbFactory mongoFactory;

	@Autowired
	private GridFsTemplate gridFsTemplate;

	@PostConstruct
	public void post() {
		LOG.info("Mongo {}", mongoFactory.getDb().getName());
		gridFsTemplate.find(null);
	}
}
