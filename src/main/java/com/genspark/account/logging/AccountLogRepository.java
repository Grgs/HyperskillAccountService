package com.genspark.account.logging;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountLogRepository extends CrudRepository<AccountLog, Long> {
}
