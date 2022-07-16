package com.genspark.account.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface UserAuthStatusRepository extends CrudRepository<UserAuthStatus, Long> {
    List<UserAuthStatus> findByEmailIgnoreCase(String email);

    @Transactional
    void deleteByEmailIgnoreCase(String email);
}
