package com.growup.ecountry.service;

import com.growup.ecountry.dto.ApiResponseDTO;
import com.growup.ecountry.dto.SeatStatusDTO;
import com.growup.ecountry.entity.*;
import com.growup.ecountry.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.lang.model.type.NullType;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatStatusService {
    private final CountryRepository countryRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SeatRepository seatRepository;
    private final SeatStatusRepository seatStatusRepository;
    //학생 자리 사용 등록
    public ApiResponseDTO<NullType> setSeatStatus(List<SeatStatusDTO> seatStatusDTOs) {
        Countries countries = countryRepository.findById(seatStatusDTOs.get(0).getCountryId()).orElseThrow(()->{ throw new IllegalArgumentException("국가가 존재하지 않습니다");});
        if(countries.getAvailable()){
            for(SeatStatusDTO seatStatusDTO : seatStatusDTOs) {
                SeatStatus seatStatus = SeatStatus.builder()
                        .rowNum(seatStatusDTO.getRowNum())
                        .colNum(seatStatusDTO.getColNum())
                        .ownerId(null)
                        .studentId(null)
                        .countryId(seatStatusDTO.getCountryId()).build();
                if(seatStatusDTO.getOwnerId() != null) {
                    Students ownerStudent = studentRepository.findById(seatStatusDTO.getOwnerId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학생입니다."));
                    if(ownerStudent.getAvailable()){
                        seatStatus.setOwnerId(ownerStudent.getId());
                    }
                    else {
                        seatStatus.setOwnerId(null);
                    }
                }
                if(seatStatusDTO.getStudentId() != null) {
                    Students student = studentRepository.findById(seatStatusDTO.getStudentId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학생입니다."));
                    if(student.getAvailable()){
                        seatStatus.setStudentId(student.getId());
                    }
                    else {
                        seatStatus.setStudentId(null);
                    }
                }
                seatStatusRepository.save(seatStatus);
            }
            return new ApiResponseDTO<>(true, "자리 등록 배정 완료", null);
        }
        else {
            return new ApiResponseDTO<>(false, "비활성화된 국가입니다", null);
        }
    }
    //자리 사용 현황 조회
    public ApiResponseDTO<List<SeatStatusDTO>> getSeatStatus2(Long countryId) {
            Countries countries = countryRepository.findById(countryId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 국가입니다."));
            if(countries.getAvailable()){
                List<SeatStatus> seatStatus = seatStatusRepository.findAllByCountryId(countries.getId());
                List<SeatStatusDTO> seatStatusDTOList = new ArrayList<>();
                for(SeatStatus seatStatus1 : seatStatus){
                    SeatStatusDTO seatStatusDTO = SeatStatusDTO.builder()
                            .id(seatStatus1.getId())
                            .rowNum(seatStatus1.getRowNum())
                            .colNum(seatStatus1.getColNum())
                            .ownerId(null)
                            .studentId(null)
                            .countryId(countries.getId()).build();
                    Students ownerStudent = studentRepository.findById(seatStatus1.getOwnerId()).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 학생입니다."));
                    if(ownerStudent.getAvailable()){
                        seatStatusDTO.setOwnerId(ownerStudent.getId());
                    }
                    Students student = studentRepository.findById(seatStatus1.getStudentId()).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 학생입니다."));
                    if(student.getAvailable()){
                        seatStatusDTO.setOwnerId(student.getId());
                    }
                    seatStatusDTOList.add(seatStatusDTO);
                }
                return new ApiResponseDTO<>(true, "자리 사용 현황", seatStatusDTOList);
            }
            else {
                return new ApiResponseDTO<>(false, "비활성화된 국가입니다", null);
            }
    }
    //자리 사용 현황 수정
    public ApiResponseDTO<NullType> updateSeatStatus(List<SeatStatusDTO> seatStatusDTOs) {
        for(SeatStatusDTO seatStatusDTO : seatStatusDTOs) {
            SeatStatus seatStatusExist = seatStatusRepository.findById(seatStatusDTO.getId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다"));
            SeatStatus seatStatus = SeatStatus.builder()
                    .id(seatStatusExist.getId())
                    .rowNum(seatStatusDTO.getRowNum())
                    .colNum(seatStatusDTO.getColNum())
                    .ownerId(null)
                    .studentId(null)
                    .countryId(seatStatusExist.getCountryId()).build();

            if(seatStatusDTO.getOwnerId() != null) {
                Students ownerStudent = studentRepository.findById(seatStatusDTO.getOwnerId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학생입니다."));
                seatStatus.setOwnerId(ownerStudent.getId());
            }
            if(seatStatusDTO.getStudentId() != null) {
                Students student = studentRepository.findById(seatStatusDTO.getStudentId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학생입니다."));
                seatStatus.setStudentId(student.getId());
            }
            seatStatusRepository.save(seatStatus);
        }
        return new ApiResponseDTO<>(true, "자리 사용 현황 업데이트 완료", null);
    }
}
