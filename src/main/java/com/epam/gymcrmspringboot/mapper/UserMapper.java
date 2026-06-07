package com.epam.gymcrmspringboot.mapper;

import com.epam.gymcrmspringboot.dto.request.CreateTraineeRequest;
import com.epam.gymcrmspringboot.dto.request.CreateTrainerRequest;
import com.epam.gymcrmspringboot.dto.request.CreateUserRequest;
import com.epam.gymcrmspringboot.dto.response.UserResponse;
import com.epam.gymcrmspringboot.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    CreateUserRequest toCreateUserRequest(CreateTraineeRequest request);

    CreateUserRequest toCreateUserRequest(CreateTrainerRequest request);

    UserResponse toResponse(UserEntity entity);

}
