package com.nova.finance.account;

import com.nova.finance.account.web.dto.AccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps the {@link Account} entity to its API response. The {@code type} enum is
 * projected as its name; audit timestamps are carried through verbatim.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "type", source = "type")
    AccountResponse toResponse(Account account);
}
