package com.epam.gymcrmspringboot.client;

import com.epam.gymcrmspringboot.config.WorkloadClientFeignConfig;
import com.epam.gymcrmspringboot.dto.request.TrainerWorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "${workload.service.name}", configuration = WorkloadClientFeignConfig.class)
public interface WorkloadClient {

    @PostMapping("/workload")
    void notifyWorkload(
            @RequestBody TrainerWorkloadRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    );
}
