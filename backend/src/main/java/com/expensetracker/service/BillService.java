package com.expensetracker.service;

import com.expensetracker.entity.Bill;
import com.expensetracker.entity.User;
import com.expensetracker.repository.BillRepository;
import com.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private AuthService authService;

    private User getCurrentUser() {
        return authService.getCurrentUser();
    }

    public List<Bill> getAllBills() {
        return billRepository.findByUserIdOrderByDueDateAsc(getCurrentUser().getId());
    }

    public Bill addBill(String title, BigDecimal amount, LocalDate dueDate) {
        Bill bill = new Bill();
        bill.setTitle(title);
        bill.setAmount(amount);
        bill.setDueDate(dueDate);
        bill.setUser(getCurrentUser());
        return billRepository.save(bill);
    }

    public Bill updateBill(Long id, String title, BigDecimal amount, LocalDate dueDate, Boolean isPaid) {
        Bill bill = billRepository.findById(id).orElseThrow(() -> new RuntimeException("Bill not found"));
        if (!bill.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Unauthorized");
        }
        bill.setTitle(title);
        bill.setAmount(amount);
        bill.setDueDate(dueDate);
        if (isPaid != null) bill.setPaid(isPaid);
        return billRepository.save(bill);
    }

    public Bill markAsPaid(Long id) {
        Bill bill = billRepository.findById(id).orElseThrow(() -> new RuntimeException("Bill not found"));
        if (!bill.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Unauthorized");
        }
        bill.setPaid(true);
        return billRepository.save(bill);
    }

    public void deleteBill(Long id) {
        Bill bill = billRepository.findById(id).orElseThrow(() -> new RuntimeException("Bill not found"));
        if (!bill.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Unauthorized");
        }
        billRepository.deleteById(id);
    }
}
