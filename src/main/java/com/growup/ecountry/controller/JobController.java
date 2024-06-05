package com.growup.ecountry.controller;


import com.growup.ecountry.dto.ApiResponseDTO;
import com.growup.ecountry.dto.JobDTO;
import com.growup.ecountry.service.JobService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.type.NullType;
import java.util.List;

@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
public class JobController {
    public final JobService jobService;

    //직업리스트 등록
    @PostMapping
    public ResponseEntity<ApiResponseDTO<NullType>> addJobs(@RequestBody List<JobRequestData> jobRequestDataList){
        boolean success = jobService.addJob(jobRequestDataList);
        String msg = success ? "직업리스트 등록에 성공하였습니다. " : "직업리스트 등록에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success, msg));
    }

    //직업리스트 조회
    @GetMapping("/{countryId}")
    public ResponseEntity<ApiResponseDTO<List<JobService.JobResponseData>>> findAllJob(@PathVariable("countryId") Long countryId){
        return ResponseEntity.ok(new ApiResponseDTO<List<JobService.JobResponseData>>(true, "직업조회에 성공하였습니다.",jobService.findAllJobs(countryId)));
    }

    //직업리스트 1개 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<NullType>> deleteJob(@PathVariable("id") Long id){
        boolean success = jobService.deleteJob(id);
        String msg = success ? "직업리스트 1개 삭제에 성공하였습니다." : "직업리스트 1개 삭제에 실패하였습니다.";
        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success,msg));

    }

    //직업리스트 1개 수정
    @PatchMapping
    public ResponseEntity<ApiResponseDTO<NullType>> updateJob(@RequestBody JobDTO jobDTO){
        boolean success = jobService.updateJob(jobDTO);
        String msg = success ? "직업리스트 1개 수정에 성공하였습니다." : "직업리스트 1개 수정에 실패하였습니다.";
        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success, msg));
    }


    @Getter
    @Setter
    public static class JobRequestData{

        private Long id;
        private String name;
        private String roll;
        private String standard;
        private Integer salary;
        private Integer limited;
        private Long countryId;
        private Integer[] skills;
    }


    }
