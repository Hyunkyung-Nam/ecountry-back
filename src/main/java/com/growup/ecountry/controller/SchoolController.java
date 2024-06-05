package com.growup.ecountry.controller;

import com.growup.ecountry.dto.ApiResponseDTO;
import com.growup.ecountry.dto.CountryDTO;
import com.growup.ecountry.service.CountryService;
import com.growup.ecountry.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.type.NullType;

@RestController
@RequestMapping("/api/school")
@RequiredArgsConstructor
public class SchoolController {
    private final CountryService countryService;
    private  final SchoolService schoolService;

    //학교 정보 조회
    @GetMapping
    public ResponseEntity<ApiResponseDTO<?>> getSchoolInfo(@RequestParam(name = "schoolName",required = false) String schoolName){

        return ResponseEntity.ok(new ApiResponseDTO<>(true, "",schoolService.schoolInfoSearchAPI(schoolName)));
    }

    //급식정보 조회
    @GetMapping("/menu/{countryId}")
    public ResponseEntity<ApiResponseDTO<?>> getSchoolMenu(@PathVariable("countryId") Long countryId){
        try {
            CountryDTO countryDTO = countryService.findCountryInfo(countryId);
            if(countryDTO == null){
                return ResponseEntity.ok(new ApiResponseDTO<NullType>(false, "국가 정보 조회에 실패하였습니다."));
            }
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "급식 조회에 성공하였습니다.",schoolService.schoolMenuSearchAPI(countryDTO)));
        }catch (Exception e){
            System.out.println("급식 조회 오류 : " + e.getMessage());
            return ResponseEntity.ok(new ApiResponseDTO<NullType>(false, "급식 조회에 실패하였습니다."));
        }
    }
    //학교 시간표 검색
    @GetMapping("/timetable/{countryId}")
    public ResponseEntity<ApiResponseDTO<?>> getSchoolTimeTable(@PathVariable("countryId") Long countryId){
        try {
            CountryDTO countryDTO = countryService.findCountryInfo(countryId);
            if(countryDTO == null){
                return ResponseEntity.ok(new ApiResponseDTO<NullType>(false, "국가 정보 조회에 실패하였습니다."));
            }
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "학교 시간표 조회에 성공하였습니다.",schoolService.schoolTimeTableSearchApi(countryDTO)));
        }catch (Exception e){
            System.out.println("학교 시간표 조회 오류 : " + e.getMessage());
            return ResponseEntity.ok(new ApiResponseDTO<NullType>(false, "학교 시간표 조회에 실패하였습니다."));
        }
    }
}
