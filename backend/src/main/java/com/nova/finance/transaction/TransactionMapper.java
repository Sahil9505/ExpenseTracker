package com.nova.finance.transaction;

import com.nova.finance.account.Account;
import com.nova.finance.category.Category;
import com.nova.finance.transaction.web.dto.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps a {@link Transaction} to its API response, inlining account and category
 * references. Enum types are projected as their name.
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "type", source = "type")
    @Mapping(target = "note", source = "description")
    @Mapping(target = "account", source = "account")
    @Mapping(target = "destinationAccount", source = "destinationAccount")
    @Mapping(target = "category", source = "category")
    TransactionResponse toResponse(Transaction transaction);

    TransactionResponse.AccountRef toAccountRef(Account account);

    TransactionResponse.CategoryRef toCategoryRef(Category category);
}
