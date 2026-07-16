package com.nova.finance.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByUserIdOrderByTypeAscNameAsc(UUID userId);

    Optional<Category> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndNameAndType(UUID userId, String name, Category.Type type);

    boolean existsByUserIdAndNameAndTypeAndIdNot(UUID userId, String name, Category.Type type, UUID id);

    boolean existsByUserIdAndSystemTrue(UUID userId);
}
