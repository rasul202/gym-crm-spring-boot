package com.epam.gymcrmspringboot.mapper;

import com.epam.gymcrmspringboot.dto.response.GetTraineeTrainingsResponse;
import com.epam.gymcrmspringboot.dto.response.GetTrainerTrainingsResponse;
import com.epam.gymcrmspringboot.dto.response.TrainingResponse;
import com.epam.gymcrmspringboot.model.TrainingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TrainingMapper {

    @Mapping(target = "trainerUsername", source = "trainer.user.username")
    @Mapping(target = "traineeUsername", source = "trainee.user.username")
    @Mapping(target = "trainingType", source = "trainingType.trainingTypeName")
    GetTrainerTrainingsResponse toGetTrainerTrainingsResponse(TrainingEntity entity);

    @Mapping(target = "trainerUsername", source = "trainer.user.username")
    @Mapping(target = "traineeUsername", source = "trainee.user.username")
    @Mapping(target = "trainingType", source = "trainingType.trainingTypeName")
    GetTraineeTrainingsResponse toGetTraineeTrainingsResponse(TrainingEntity entity);
}
