package com.epam.gymcrmspringboot.mapper;

import com.epam.gymcrmspringboot.dto.response.GetTrainerProfileResponse;
import com.epam.gymcrmspringboot.dto.response.TraineeSummary;
import com.epam.gymcrmspringboot.dto.response.TrainerResponse;
import com.epam.gymcrmspringboot.dto.response.TrainerSummary;
import com.epam.gymcrmspringboot.dto.response.UpdateTrainerProfileResponse;
import com.epam.gymcrmspringboot.model.TraineeEntity;
import com.epam.gymcrmspringboot.model.TrainerEntity;
import com.epam.gymcrmspringboot.model.TrainingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring",
        uses = {UserMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TrainerMapper {

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "trainees", expression = "java(toTraineeSummaries(trainer))")
    GetTrainerProfileResponse toGetTrainerProfileResponse(TrainerEntity trainer);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "trainees", expression = "java(toTraineeSummaries(trainer))")
    UpdateTrainerProfileResponse toUpdateTrainerProfileResponse(TrainerEntity trainer);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    TrainerSummary trainerEntityToTrainerSummary(TrainerEntity trainer);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    TraineeSummary traineeEntityToTraineeSummary(TraineeEntity entity);

    default List<TraineeSummary> toTraineeSummaries(TrainerEntity entity) {
        if (entity == null || entity.getTrainings() == null) {
            return Collections.emptyList();
        }

        return entity.getTrainings().stream()
                .map(TrainingEntity::getTrainee)
                .filter(Objects::nonNull)
                .filter(trainee -> trainee.getUser() != null && Boolean.TRUE.equals(trainee.getUser().getIsActive()))
                .distinct()
                .map(this::traineeEntityToTraineeSummary)
                .toList();
    }
}
