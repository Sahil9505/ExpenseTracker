package com.nova.finance.category;

import com.nova.common.exception.BadRequestException;
import com.nova.common.exception.ConflictException;
import com.nova.common.exception.ResourceNotFoundException;
import com.nova.finance.category.web.dto.CategoryResponse;
import com.nova.finance.category.web.dto.CreateCategoryRequest;
import com.nova.finance.category.web.dto.UpdateCategoryRequest;
import com.nova.finance.transaction.TransactionRepository;
import com.nova.user.User;
import com.nova.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Category lifecycle and the default-category seed that runs when a user
 * registers. Names are unique per (user, type); system categories are protected
 * from deletion and renaming. A category that is still referenced by transactions
 * cannot be deleted, so historical categorization is preserved.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;
    private final TransactionRepository transactionRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            CategoryMapper categoryMapper,
            TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.categoryMapper = categoryMapper;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories(UUID userId) {
        return categoryRepository.findByUserIdOrderByTypeAscNameAsc(userId).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(UUID userId, UUID id) {
        return categoryMapper.toResponse(loadOwned(id, userId));
    }

    @Transactional
    public CategoryResponse createCategory(UUID userId, CreateCategoryRequest request) {
        User user = loadUser(userId);
        String name = request.name().trim();
        if (categoryRepository.existsByUserIdAndNameAndType(userId, name, request.type())) {
            throw new ConflictException(
                    "You already have a " + request.type().name().toLowerCase() + " category named '" + name + "'.");
        }
        Category category = new Category(user, name, request.type());
        category.setColor(blankToNull(request.color()));
        category.setIcon(blankToNull(request.icon()));
        category.setSystem(false);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(UUID userId, UUID id, UpdateCategoryRequest request) {
        Category category = loadOwned(id, userId);
        if (request.name() != null && !request.name().isBlank()) {
            if (category.isSystem()) {
                throw new ConflictException("System categories cannot be renamed.");
            }
            String name = request.name().trim();
            if (!name.equals(category.getName())
                    && categoryRepository.existsByUserIdAndNameAndTypeAndIdNot(userId, name, category.getType(), id)) {
                throw new ConflictException(
                        "You already have a " + category.getType().name().toLowerCase() + " category named '" + name + "'.");
            }
            category.setName(name);
        }
        if (request.color() != null) {
            category.setColor(blankToNull(request.color()));
        }
        if (request.icon() != null) {
            category.setIcon(blankToNull(request.icon()));
        }
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(UUID userId, UUID id) {
        Category category = loadOwned(id, userId);
        if (category.isSystem()) {
            throw new ConflictException("System categories cannot be deleted.");
        }
        long usage = transactionRepository.countByCategoryId(id);
        if (usage > 0) {
            throw new BadRequestException(
                    "This category is used by " + usage + " transaction" + (usage == 1 ? "" : "s")
                            + " and cannot be deleted.");
        }
        categoryRepository.delete(category);
    }

    /**
     * Seeds the standard starter categories for a newly registered user. Safe to
     * call repeatedly: if the user already has system categories nothing happens.
     */
    @Transactional
    public void seedDefaultsForUser(UUID userId) {
        if (categoryRepository.existsByUserIdAndSystemTrue(userId)) {
            return;
        }
        User user = loadUser(userId);
        List<Category> defaults = DEFAULT_CATEGORIES.stream().map(def -> {
            Category category = new Category(user, def.name(), def.type());
            category.setColor(def.color());
            category.setIcon(def.icon());
            category.setSystem(true);
            return category;
        }).toList();
        categoryRepository.saveAll(defaults);
    }

    private Category loadOwned(UUID id, UUID userId) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id.toString()));
    }

    private User loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    /** Starter categories seeded on registration, grouped by type. */
    private static final List<DefaultCategory> DEFAULT_CATEGORIES = List.of(
            new DefaultCategory("Food", Category.Type.EXPENSE, "#F59E0B", "utensils"),
            new DefaultCategory("Transport", Category.Type.EXPENSE, "#38BDF8", "car"),
            new DefaultCategory("Shopping", Category.Type.EXPENSE, "#60A5FA", "shopping-bag"),
            new DefaultCategory("Bills", Category.Type.EXPENSE, "#3B82F6", "receipt"),
            new DefaultCategory("Health", Category.Type.EXPENSE, "#10B981", "heart-pulse"),
            new DefaultCategory("Entertainment", Category.Type.EXPENSE, "#A855F7", "film"),
            new DefaultCategory("Travel", Category.Type.EXPENSE, "#06B6D4", "plane"),
            new DefaultCategory("Education", Category.Type.EXPENSE, "#8B5CF6", "graduation-cap"),
            new DefaultCategory("Other", Category.Type.EXPENSE, "#94A3B8", "tag"),
            new DefaultCategory("Salary", Category.Type.INCOME, "#10B981", "banknote"),
            new DefaultCategory("Freelance", Category.Type.INCOME, "#34D399", "briefcase"),
            new DefaultCategory("Gift", Category.Type.INCOME, "#F472B6", "gift"),
            new DefaultCategory("Refund", Category.Type.INCOME, "#60A5FA", "rotate-ccw"),
            new DefaultCategory("Investment", Category.Type.INCOME, "#22D3EE", "trending-up"),
            new DefaultCategory("Other", Category.Type.INCOME, "#94A3B8", "tag")
    );

    private record DefaultCategory(String name, Category.Type type, String color, String icon) {
    }
}
