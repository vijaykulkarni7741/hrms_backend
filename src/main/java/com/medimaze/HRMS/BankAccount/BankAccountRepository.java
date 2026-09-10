package com.medimaze.HRMS.BankAccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByEmployeeId(Long employeeId);
    Optional<BankAccount> findByAccountNumber(String accountNumber);
}
