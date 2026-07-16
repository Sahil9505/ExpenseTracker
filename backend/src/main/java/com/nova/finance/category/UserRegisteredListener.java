package com.nova.finance.category;

import com.nova.user.event.UserRegisteredEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Provisions starter categories for a user once their registration transaction
 * has committed. Runs in its own transaction so a failure here can't roll back
 * the registration, and is idempotent for safety on repeated events.
 */
@Component
public class UserRegisteredListener {

    private final CategoryService categoryService;

    public UserRegisteredListener(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void onUserRegistered(UserRegisteredEvent event) {
        categoryService.seedDefaultsForUser(event.getUserId());
    }
}
