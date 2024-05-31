package com.growup.ecountry.service;

import com.growup.ecountry.config.TokenProvider;
import com.growup.ecountry.dto.ApiResponseDTO;
import com.growup.ecountry.dto.CountryDTO;
import com.growup.ecountry.entity.AccountLists;
import com.growup.ecountry.entity.Countries;
import com.growup.ecountry.entity.Jobs;
import com.growup.ecountry.entity.Users;
import com.growup.ecountry.repository.AccountListRepository;
import com.growup.ecountry.repository.CountryRepository;
import com.growup.ecountry.repository.JobRepository;
import com.growup.ecountry.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.lang.model.type.NullType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CountryService {
    private final CountryRepository countryRepository;
    private final UserRepository userRepository;
    private final AccountListRepository accountListRepository;
    private final JobRepository jobRepository;
    private final TokenProvider jwt;

    //국가정보 조회
    public ApiResponseDTO<CountryDTO> findCountries(Long countryId) {
        Optional<Countries> countryExist = countryRepository.findById(countryId);
        if (countryExist.isPresent()) {
            Countries country = countryExist.get();
            if(country.getAvailable()) {
                CountryDTO countryDTO = CountryDTO.builder()
                        .school(country.getSchool())
                        .name(country.getName())
                        .grade(country.getGrade())
                        .classroom(country.getClassroom())
                        .unit(country.getUnit())
                        .treasury(country.getTreasury())
                        .salaryDate(country.getSalaryDate())
                        .available(country.getAvailable())
                        .build();
                return new ApiResponseDTO<>(true, "국가 리스트 조회 성공", countryDTO);
            }
            else {
                return new ApiResponseDTO<>(false, "비활성화된 국가입니다", null);
            }
        } else {
            return new ApiResponseDTO<>(false, "국가가 존재하지 않습니다", null);
        }
    }
    //1개 국가 정보 조회
    public CountryDTO findCountryInfo(Long countryId){
        Countries country = countryRepository.findById(countryId).orElseThrow();
        try {
            if(country.getAvailable()){
                return CountryDTO.builder().id(country.getId()).school(country.getSchool()).grade(country.getGrade()).classroom(country.getClassroom()).eduOfficeCode(country.getEduOfficeCode()).schoolCode(country.getSchoolCode()).build();
            }
            else {
                System.out.println("1개 국가 정보 조회 오류 : 비활성화된 국가입니다.");
                return null;
            }
        }
        catch (Exception e){
            System.out.println("1개 국가 정보 조회 오류 : "+e.getMessage());
            return null;
        }
    }
    //국가생성
     public ApiResponseDTO<Long> create(CountryDTO countryDTO, Long id){
        Optional<Users> user = userRepository.findById(id);
         System.out.print(user);
        if(user.isPresent()){
            Users users = user.get();
            Countries countries = Countries.builder()
                    .school(countryDTO.getSchool())
                    .name(countryDTO.getName())
                    .grade(countryDTO.getGrade())
                    .classroom(countryDTO.getClassroom())
                    .unit(countryDTO.getUnit())
                    .eduOfficeCode(countryDTO.getEduOfficeCode())
                    .schoolCode(countryDTO.getSchoolCode())
                    .salaryDate(countryDTO.getSalaryDate())
                    .userId(users.getId()).build();
            countryRepository.save(countries);
            accountListRepository.save(AccountLists.builder().division(false).name("입출금 통장").interest(0.0).available(true).countryId(countries.getId()).build());
            return  new ApiResponseDTO<>(true,"국가 생성 완료",countries.getId());
        }
        else {
            return  new ApiResponseDTO<>(false,"국가 생성 실패",null);
        }
    }

    //국가삭제(비활성화)
      public ApiResponseDTO<NullType> delete(Long id){
        Countries country = countryRepository.findById(id).orElseThrow(()->new IllegalArgumentException("국가가 존재하지 않습니다"));
        if(country.getAvailable()){
            country.setAvailable(false);
            countryRepository.save(country);
        }
        return new ApiResponseDTO<>(true,"국가 삭제 성공",null);
      }

      //국고 조회
    public CountryDTO findTreasury(Long countryId){
        Countries country = countryRepository.findById(countryId).orElseThrow();
        CountryDTO countryDTO = null  ;
        try {
            if(country.getAvailable()){
                countryDTO = CountryDTO.builder().treasury(country.getTreasury()).build();
            }
            else {
                System.out.println("국고 조회 오류 : 비활성화된 국가입니다.");
                return countryDTO;
            }
        }
        catch (Exception e){
            System.out.println("국고 조회 오류 : "+e.getMessage());
            return countryDTO;
        }
        return countryDTO;
    }


      //국고 수정
    public Boolean updateTreasury(Long countryId,Integer treasury){
        Countries country = countryRepository.findById(countryId).orElseThrow();
        try {
            if(country.getAvailable()){
                country.setTreasury(treasury);
                countryRepository.save(country);
            }
            else {
                System.out.println("국고 수정 오류 : 비활성화된 국가입니다.");
                return false;
            }
        }catch (Exception e){
            System.out.println("국고 수정 오류  : "  + e.getMessage());
            return false;
        }
        return true;
    }
}
