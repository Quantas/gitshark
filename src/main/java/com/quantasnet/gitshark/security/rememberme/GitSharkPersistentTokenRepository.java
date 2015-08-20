package com.quantasnet.gitshark.security.rememberme;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class GitSharkPersistentTokenRepository implements PersistentTokenRepository {

    private static final Logger LOG = LoggerFactory.getLogger(GitSharkPersistentTokenRepository.class);

    @Autowired
    private RememberMeTokenRepo rememberMeTokenRepository;

    @Override
    public void createNewToken(final PersistentRememberMeToken token) {
        final RememberMeToken rememberMeToken = convert(token);

        try {
            rememberMeTokenRepository.save(rememberMeToken);
        } catch (final DataAccessException dae) {
            LOG.error("Error storing new Remember Me Token", dae);
        }
    }

    @Override
    public void updateToken(final String series, final String tokenValue, final Date lastUsed) {
        try {
        	final RememberMeToken existingToken = rememberMeTokenRepository.findOne(series);
        	if (null != existingToken) {
        		existingToken.setToken(tokenValue);
        		existingToken.setLastUsed(lastUsed);
        		rememberMeTokenRepository.save(existingToken);
        	}
        } catch (final DataAccessException dae) {
            LOG.error("Error updating Remember Me token", dae);
        }
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(final String seriesId) {
        try {
            final RememberMeToken rememberMeToken = rememberMeTokenRepository.findOne(seriesId);

            if (null != rememberMeToken) {
                return convert(rememberMeToken);
            }
        } catch (final DataAccessException dae) {
            LOG.error("Error retrieving Remember Me token", dae);
        }

        return null;
    }

    @Override
    public void removeUserTokens(final String username) {
        try {
            rememberMeTokenRepository.deleteRememberMeTokenByUsername(username);
        } catch (final DataAccessException dae) {
            LOG.error("Error removing all previous Remember Me tokens", dae);
        }
    }

    protected RememberMeToken convert(final PersistentRememberMeToken persistentRememberMeToken) {
        final RememberMeToken rememberMeToken = new RememberMeToken();
        rememberMeToken.setToken(persistentRememberMeToken.getTokenValue());
        rememberMeToken.setId(persistentRememberMeToken.getSeries());
        rememberMeToken.setUsername(persistentRememberMeToken.getUsername());
        rememberMeToken.setLastUsed(persistentRememberMeToken.getDate());
        return rememberMeToken;
    }

    protected PersistentRememberMeToken convert(final RememberMeToken rememberMeToken) {
        return new PersistentRememberMeToken(rememberMeToken.getUsername(),
                rememberMeToken.getId(), rememberMeToken.getToken(),
                rememberMeToken.getLastUsed());
    }
}
