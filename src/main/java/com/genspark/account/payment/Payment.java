package com.genspark.account.payment;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "Payment", uniqueConstraints = {
        @UniqueConstraint(name = "uc_payment_employee_period", columnNames = {"employee", "period"})
})
public class Payment {
    @Transient
    private final Pattern salaryPattern = Pattern.compile("^\\D*(\\d+)\\D+(\\d+)\\D*$");
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;
    @Email
    @NotNull
    @Column(nullable = false)
    private String employee;
    private YearMonth period;
    @Column(nullable = false)
    private BigDecimal salary;

    public Payment() {
    }

    public Payment(String employee, YearMonth period, BigDecimal salary) {
        this.employee = employee;
        this.period = period;
        this.salary = salary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String email) {
        this.employee = email;
    }


    public YearMonth getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = YearMonth.parse(period, DateTimeFormatter.ofPattern("[MM-yyyy][MMMM-yyyy]"));
    }

    public void setPeriod(YearMonth period) {
        this.period = period;
    }

    public String getFormattedPeriod() {
        return period.format(DateTimeFormatter.ofPattern("MMMM-yyyy"));
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(Object inputValue) {
        String value = String.valueOf(inputValue);
        Matcher m = salaryPattern.matcher(value);
        if (m.find()) {
            this.salary = new BigDecimal(m.group(1) + "." + m.group(2));
        } else if (value.matches("\\d+")) {
            if (Math.abs(Long.parseLong(value)) < 100) {
                this.salary = new BigDecimal(value).divide(new BigDecimal(100), new MathContext(2));
            } else {
                this.salary = new BigDecimal(value.substring(0, value.length() - 2) + "." +
                        value.substring(value.length() - 2));
            }

        } else {
            try {
                this.salary = new BigDecimal(value);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to convert salary");

            }
        }
    }

    public String getFormattedSalary() {
        if (this.salary.toString().matches("\\d+")) {
            return String.format("%s dollar(s) 0 cent(s)", this.salary.toString());
        }
        String[] salaryParts = this.salary.toString().split("\\.");
        return String.format("%s dollar(s) %s cent(s)", salaryParts[0], salaryParts[1]);
    }

}
