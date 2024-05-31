package com.growup.ecountry.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.growup.ecountry.config.TokenProvider;
import com.growup.ecountry.dto.ApiResponseDTO;
import com.growup.ecountry.dto.SeatStatusDTO;
import com.growup.ecountry.dto.TokenDTO;
import com.growup.ecountry.entity.Students;
import com.growup.ecountry.repository.StudentRepository;
import com.growup.ecountry.service.SeatStatusService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.type.NullType;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/seat/status")
@RequiredArgsConstructor
public class SeatStatusController {
    private final SeatStatusService seatStatusService;
    private final StudentRepository studentRepository;
    private final TokenProvider jwt;
    //학생 자리 사용 등록
    @PostMapping
    public ResponseEntity<ApiResponseDTO<NullType>> setSeatStatus(@RequestBody List<SeatStatusDTO> seatStatusDTO) {
        return ResponseEntity.ok(seatStatusService.setSeatStatus(seatStatusDTO));
    }
    //자리사용현황조회
    @GetMapping("/{countryId}")
    public ResponseEntity<ApiResponseDTO<List<SeatStatusData>>> getStatus2(@PathVariable("countryId") Long countryId){
        try {
                ApiResponseDTO<List<SeatStatusDTO>> apiData = seatStatusService.getSeatStatus2(countryId);
                if(apiData.getResult() != null ){
                    List<SeatStatusDTO> seatStatusDTOS = apiData.getResult();
                    List<SeatStatusData> seatStatusDataList = new ArrayList<>();
                    for(SeatStatusDTO seatStatusDTO : seatStatusDTOS) {
                        SeatStatusData seatStatusData = SeatStatusData.builder()
                                .id(seatStatusDTO.getId())
                                .rowNum(seatStatusDTO.getRowNum())
                                .colNum(seatStatusDTO.getColNum())
                                .ownerId(null)
                                .studentId(null)
                                .ownerName(null)
                                .studentName(null).build();
                        if(seatStatusDTO.getOwnerId() != null){
                            Students ownerStudent = studentRepository.findById(seatStatusDTO.getOwnerId()).orElseThrow(() -> new RuntimeException("학생이 존재하지 않습니다"));
                            seatStatusData.setOwnerId(ownerStudent.getId());
                            seatStatusData.setOwnerName(ownerStudent.getName());
                        }
                        if(seatStatusDTO.getStudentId() != null){
                            Students student = studentRepository.findById(seatStatusDTO.getStudentId()).orElseThrow(() -> new RuntimeException("학생이 존재하지 않습니다"));
                            seatStatusData.setStudentId(student.getId());
                            seatStatusData.setStudentName(student.getName());
                        }
                        seatStatusDataList.add(seatStatusData);
                    }
                    return ResponseEntity.ok(new ApiResponseDTO<>(apiData.getSuccess(),apiData.getMessage(),seatStatusDataList));
                }
                else {
                    return ResponseEntity.ok(new ApiResponseDTO<>(false, "비활성화된 국가입니다",null));
                }
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false, e.getMessage(),null));
        }
    }
    //자리 사용 현황 수정
    @PatchMapping
    public ResponseEntity<ApiResponseDTO<NullType>> updateSeatStatus(@RequestBody List<SeatStatusDTO> seatStatusDTO) {
        return ResponseEntity.ok(seatStatusService.updateSeatStatus(seatStatusDTO));
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class SeatStatusData {
        @JsonProperty
        private Long id;
        @JsonProperty
        private Integer rowNum;
        @JsonProperty
        private Integer colNum;
        @JsonProperty
        private Long ownerId;
        @JsonProperty
        private Long studentId;
        @JsonProperty
        private String ownerName;
        @JsonProperty
        private String studentName;
    }
}
