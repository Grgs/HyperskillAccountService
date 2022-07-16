package com.genspark.account.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByEmployeeAndPeriod(String employee, YearMonth period);

    List<Payment> findByEmployeeIgnoreCase(String username);

    List<Payment> findByEmployeeIgnoreCaseAndPeriod(String username, YearMonth period);
}
