package com.epam.gymcrmspringboot.mapper;

import com.epam.gymcrmspringboot.dto.response.GetTraineeProfileResponse;
import com.epam.gymcrmspringboot.dto.response.TraineeResponse;
import com.epam.gymcrmspringboot.dto.response.TrainerSummary;
import com.epam.gymcrmspringboot.dto.response.UpdateTraineeProfileResponse;
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
public interface TraineeMapper {

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "trainers", expression = "java(toTrainerSummaries(entity))")
    GetTraineeProfileResponse toGetTraineeProfileResponse(TraineeEntity entity);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "trainers", expression = "java(toTrainerSummaries(entity))")
    UpdateTraineeProfileResponse toUpdateTraineeProfileResponse(TraineeEntity entity);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    TrainerSummary trainerEntityToTrainerSummary(TrainerEntity entity);

    default List<TrainerSummary> toTrainerSummaries(TraineeEntity entity) {
        if (entity == null || entity.getTrainings() == null) {
            return Collections.emptyList();
        }

        return entity.getTrainings().stream()
                .map(TrainingEntity::getTrainer)
                .filter(Objects::nonNull)
                .filter(trainer -> trainer.getUser() != null && Boolean.TRUE.equals(trainer.getUser().getIsActive()))
                .distinct()
                .map(this::trainerEntityToTrainerSummary)
                .toList();
    }
}
