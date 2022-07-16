package com.genspark.account.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByEmailIgnoreCase(String email);

    @Transactional
    void deleteByEmailIgnoreCase(String email);
}
