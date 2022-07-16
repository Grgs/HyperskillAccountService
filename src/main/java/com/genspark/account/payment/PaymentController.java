package com.genspark.account.payment;

import com.genspark.account.exceptions.EmployeeNotFoundException;
import com.genspark.account.user.User;
import com.genspark.account.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
public class PaymentController {
    private final Pattern salaryPattern = Pattern.compile("^\\D*(\\d+)\\D+(\\d+)\\D*$");
    @Autowired
    UserRepository userRepository;
    @Autowired
    PaymentRepository paymentRepository;

    @GetMapping("/api/empl/payment")
    List<PaymentOutput> getPayments(@AuthenticationPrincipal UserDetails userDetails) {
        List<Payment> payments = paymentRepository.findByEmployeeIgnoreCase(userDetails.getUsername());
        User user = this.getUser(userDetails.getUsername());
        List<PaymentOutput> response = new ArrayList<>();
        for (Payment payment : payments) {
            PaymentOutput responsePayment = new PaymentOutput();
            responsePayment.setName(user.getName());
            responsePayment.setLastname(user.getLastname());
            responsePayment.setPeriod(payment.getPeriod().
                    format(DateTimeFormatter.ofPattern("MMMM-yyyy")));
            responsePayment.setSalary(payment.getFormattedSalary());
            response.add(0, responsePayment);
        }
        return response;
    }

    @GetMapping(value = "/api/empl/payment", params = "period")
    PaymentOutput getPayment(@RequestParam String period, @AuthenticationPrincipal UserDetails userDetails) {
        YearMonth ymPeriod;
        try {
            ymPeriod = YearMonth.parse(period, DateTimeFormatter.ofPattern("[MM-yyyy][MMMM-yyyy]"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error!");
        }
        List<Payment> payments = paymentRepository.findByEmployeeIgnoreCaseAndPeriod(
                userDetails.getUsername(), ymPeriod);
        if (payments.size() < 1) {
            throw new EmployeeNotFoundException();
        }
        Payment payment = payments.get(0);
        User user = this.getUser(userDetails.getUsername());
        PaymentOutput responsePayment = new PaymentOutput();
        responsePayment.setName(user.getName());
        responsePayment.setLastname(user.getLastname());
        responsePayment.setPeriod(payment.getPeriod().
                format(DateTimeFormatter.ofPattern("MMMM-yyyy")));
        responsePayment.setSalary(payment.getFormattedSalary());
        return responsePayment;
    }

    @Transactional
    @PostMapping("/api/acct/payments")
    Map<String, String> addPayments(@Valid @RequestBody List<Payment> payments) {
        for (Payment payment : payments) {
            if (paymentRepository.findByEmployeeAndPeriod(payment.getEmployee(), payment.getPeriod()).size() > 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error!");
            if (payment.getSalary().compareTo(BigDecimal.valueOf(0)) < 1)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error!");
            paymentRepository.save(payment);
        }
        return Map.of("status", "Added successfully!");
    }

    @Transactional
    @PutMapping("/api/acct/payments")
    Map<String, String> changePayments(@RequestBody Payment payment) {
        List<Payment> storedPayments = paymentRepository.
                findByEmployeeAndPeriod(payment.getEmployee(), payment.getPeriod());
        if (storedPayments.size() < 1)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment was not found!");
        Payment storedPayment = storedPayments.get(0);
        storedPayment.setSalary(payment.getSalary());
        paymentRepository.save(storedPayment);
        return Map.of("status", "Updated successfully!");
    }

    private User getUser(String email) {
        List<User> userList = userRepository.findByEmailIgnoreCase(email);
        if (userList.size() == 0) {
            throw new EmployeeNotFoundException();
        }
        return userList.get(0);
    }
}
