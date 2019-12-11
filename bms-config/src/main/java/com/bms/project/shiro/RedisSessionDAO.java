package com.bms.project.shiro;

import com.bms.project.model.ResponseCode;
import com.bms.project.spring.BmsException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author liqiang
 * @date 2019/11/13 22:47
 */
@Slf4j
public class RedisSessionDAO extends AbstractSessionDAO {



    @Override
    protected Serializable doCreate(Session session) {
        if (session == null) {
            log.error("session is null");
            throw new BmsException(ResponseCode.SESSION_CANNOT_BE_NULL);
        }
        Serializable sessionId = this.generateSessionId(session);
        this.assignSessionId(session, sessionId);
//        this.saveSession(session);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable serializable) {
        return null;
    }

    @Override
    public void update(Session session) throws UnknownSessionException {

    }

    @Override
    public void delete(Session session) {

    }

    @Override
    public Collection<Session> getActiveSessions() {
        return null;
    }
}
