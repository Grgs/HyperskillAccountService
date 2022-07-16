package com.genspark.account.logging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountLogController {
    @Autowired
    AccountLogRepository accountLogRepository;

    @GetMapping("/api/security/events")
    Iterable<AccountLog> getLogs() {
        return accountLogRepository.findAll();
    }

}
