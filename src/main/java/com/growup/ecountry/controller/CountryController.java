package com.growup.ecountry.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.growup.ecountry.config.TokenProvider;
import com.growup.ecountry.dto.ApiResponseDTO;
import com.growup.ecountry.dto.CountryDTO;
import com.growup.ecountry.dto.TokenDTO;
import com.growup.ecountry.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.type.NullType;
import java.util.List;

@RestController
@RequestMapping("/api/country")
@RequiredArgsConstructor
public class CountryController {
    private final CountryService countryService;
    private final TokenProvider jwt;

    //국가정보 조회
    @GetMapping("/{countryId}")
    public ResponseEntity<ApiResponseDTO<JSONObject>> findAllCountries(@PathVariable("countryId") Long countryId){
        try {
                ApiResponseDTO<CountryDTO> apiData = countryService.findCountries(countryId);
                CountryDTO countryDTO = apiData.getResult();
                return ResponseEntity.ok(new ApiResponseDTO<>(apiData.getSuccess(),apiData.getMessage(), setCountryData(countryDTO)));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"국가 정보 조회 실패"));
        }
    }

    //국가생성
     @PostMapping
     public ResponseEntity<ApiResponseDTO<CountryData2>> create(@RequestHeader(value = "Authorization") String token, @RequestBody CountryDTO countryDTO){
        try {
            TokenDTO authToken = jwt.validateToken(token);
            if(authToken.getId() != 0) {
                ApiResponseDTO<Long> apiData = countryService.create(countryDTO, authToken.getId());
                return ResponseEntity.ok(new ApiResponseDTO<>(apiData.getSuccess(), apiData.getMessage(),  new CountryData2(apiData.getResult())));
            }
            else {
                return ResponseEntity.ok(new ApiResponseDTO<>(false,"국가 생성 실패",null));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"국가 생성 실패",null));
        }
     }

    //국가삭제(비활성화)
     @DeleteMapping("/{id}")
     public ResponseEntity<ApiResponseDTO<NullType>> delete(@PathVariable("id") Long id){
        try{
            return ResponseEntity.ok(countryService.delete(id));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"국가 삭제 실패",null));
        }
    }

    //국고 수정
    @PatchMapping("/treasury/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> updateTreasury(@PathVariable("countryId") Long countryId,@RequestBody CountryDTO countryDTO){
        boolean success = countryService.updateTreasury(countryId, countryDTO.getTreasury());
        String msg = success ? "국고 수정에 성공하였습니다." : "국고 수정에 실패하였습니다.";
        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success, msg));
    }

    public JSONObject setCountryData(CountryDTO countryDTO){
        JSONObject responseData = new JSONObject();
        responseData.put("name", countryDTO.getName());
        responseData.put("grade", countryDTO.getGrade());
        responseData.put("classroom", countryDTO.getClassroom());
        responseData.put("unit", countryDTO.getUnit());
        responseData.put("salaryDate",countryDTO.getSalaryDate());
        responseData.put("school",countryDTO.getSchool());
        return responseData;

    }

    static class CountryData2 {
        @JsonProperty
        private final Long id;
        public CountryData2(Long id) {
            this.id = id;
        }
    }
}
