package com.genspark.account.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
@Scope(scopeName = "prototype")
public class AccountLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountLogger.class);
    private final AccountLogRepository accountLogRepository;

    private String action;
    private String subject;
    private String object;
    private String path;

    @Autowired
    public AccountLogger(AccountLogRepository repository) {
        this.accountLogRepository = repository;
    }

    private void dbSave() {
        AccountLog accountLog = new AccountLog();
        accountLog.setDate(new Timestamp(System.currentTimeMillis()).toString());
        accountLog.setPath(this.path);
        accountLog.setAction(this.action);
        accountLog.setSubject(this.subject);
        accountLog.setObject(this.object);
        accountLogRepository.save(accountLog);
    }

    public void log(LOG_ACTIONS action, String subject, String object, String path) {
        this.action = action.toString();
        this.object = object;
        this.subject = subject == null ? "Anonymous" : subject;
        this.path = path;
        this.dbSave();
        this.consoleLog();
    }

    public void consoleLog(LOG_ACTIONS action, String subject, String object, String path) {
        this.action = action.toString();
        this.object = object;
        this.subject = subject == null ? "Anonymous" : subject;
        this.path = path;
        this.consoleLog();
    }

    private void consoleLog() {
        LOGGER.info("action: {}, subject: {}, object: {}, path: {}", this.action, this.subject, this.object, this.path);
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
