package com.growup.ecountry.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.growup.ecountry.config.TokenProvider;
import com.growup.ecountry.dto.*;
import com.growup.ecountry.entity.Countries;
import com.growup.ecountry.entity.Jobs;
import com.growup.ecountry.repository.CountryRepository;
import com.growup.ecountry.repository.JobRepository;
import com.growup.ecountry.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.type.NullType;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CountryRepository countryRepository;
    private final JobRepository jobRepository;
    private final TokenProvider jwt;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDTO<NullType>> signup(@RequestBody UserDTO userDTO) {
        try {
            Boolean result = userService.create(userDTO);
            String msg = result ? "회원가입에 성공하셨습니다" : "이미 존재하는 회원입니다";
            return ResponseEntity.ok(new ApiResponseDTO<>(result,msg,null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"회원가입 실패",null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<UserData2>> login(@RequestBody UserDTO userDTO) {
        try {
            ApiResponseDTO<Long> result = userService.login(userDTO);
            Token token = result.getSuccess() ? new Token(jwt.generateToken(result.getResult(), false))
                    : new Token(null);
            return ResponseEntity.ok(new ApiResponseDTO<>(result.getSuccess(), result.getMessage(), new UserData2(token.getToken())));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false,e.getMessage(),null));
        }
    }
    //선생님/학생 개인정보조회
    @GetMapping("/info")
    public ResponseEntity<ApiResponseDTO<?>> userInfo(@RequestHeader(value = "Authorization") String token) {
        try {
            TokenDTO authToken = jwt.validateToken(token);
            if (authToken.getId() != 0) {
                ApiResponseDTO<?> apiData = userService.userInfo(authToken.getId(), authToken.getIsStudent());
                Object result = apiData.getResult();
                if (result instanceof UserDTO) {
                    UserDTO userDTO = (UserDTO) result;
                    UserData userData = new UserData(userDTO.getId(), userDTO.getName(), userDTO.getUserId(), userDTO.getImg());
                    return ResponseEntity.ok(new ApiResponseDTO<>(true, apiData.getMessage(), userData));
                } else if (result instanceof StudentDTO) {
                    StudentDTO studentDTO = (StudentDTO) result;
                    //jobName: 무직, jobId: null, skills: null
                    if(studentDTO.getJobId() == null) {
                        StudentData studentData = new StudentData(studentDTO.getId(), studentDTO.getName(), studentDTO.getRollNumber(), studentDTO.getRating(),studentDTO.getImg(),"무직",studentDTO.getJobId(), studentDTO.getCountryId(),null);
                        return ResponseEntity.ok(new ApiResponseDTO<>(true, apiData.getMessage(), studentData));
                    }
                    Optional<Jobs> jobExist = jobRepository.findById(studentDTO.getJobId());
                    if (jobExist.isPresent()) {
                        Jobs studentJob = jobExist.get();
                        StudentData studentData = new StudentData(studentDTO.getId(), studentDTO.getName(), studentDTO.getRollNumber(), studentDTO.getRating(),studentDTO.getImg(),studentJob.getName(),studentDTO.getJobId(), studentDTO.getCountryId(),studentJob.getSkills());
                        return ResponseEntity.ok(new ApiResponseDTO<>(true, apiData.getMessage(), studentData));
                    }
                }
            }
            return ResponseEntity.ok(new ApiResponseDTO<>(false, "인증 실패", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false, "인증 실패", null));
        }
    }

    @GetMapping("/auth")
    public ResponseEntity<ApiResponseDTO<TokenDTO>> authUser(@RequestHeader(value = "Authorization") String token) {
        TokenDTO authToken = jwt.validateToken(token);
        if(authToken.getId() == 0) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false, "사용자 인증 실패", null));
        } else {
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "사용자 인증 완료", authToken));
        }
    }
    //국가 인증
    @GetMapping("/country/auth/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> authCountry(@PathVariable("countryId") Long countryId) {
        Countries countries = countryRepository.findById(countryId).orElseThrow(() -> new IllegalArgumentException("국가가 존재하지 않습니다"));
        if(countries.getAvailable()){
            return ResponseEntity.ok(new ApiResponseDTO<>(true,"국가 인증 완료",null));
        }
        else {
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"국가 인증 실패 : 비활성화된 국가입니다",null));
        }
    }

    @PatchMapping("/change")
    public ResponseEntity<ApiResponseDTO<NullType>> pwUpdate(@RequestHeader(value= "Authorization") String token, @RequestBody UserDTO userDTO) {
        try {
            TokenDTO authToken = jwt.validateToken(token);
            if(authToken.getId() != 0){
                Boolean result = userService.pwUpdate(authToken.getId(), userDTO.getPw());
                String msg = result ? "비밀번호를 성공적으로 변경하였습니다" : "비밀번호 변경에 실패하였습니다";
                return ResponseEntity.ok(new ApiResponseDTO<>(result,msg,null));
            }
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"비밀번호 변경에 실패하였습니다",null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"비밀번호 변경에 실패하였습니다",null));
        }
    }
    @PostMapping("/delete")
    public ResponseEntity<ApiResponseDTO<NullType>> deleteUser(@RequestHeader(value= "Authorization") String token) {
        try {
            TokenDTO authToken = jwt.validateToken(token);
            if(authToken.getId() != 0){
                return ResponseEntity.ok(userService.deleteUser(authToken.getId()));
            }
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"유저 삭제에 실패하였습니다",null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"유저 삭제에 실패하였습니다",null));
        }
    }


    //이미지 변경
    @PatchMapping
    public ResponseEntity<ApiResponseDTO<NullType>> imgUpdate(@RequestBody UserDTO userDTO){
        try {
            Boolean result = userService.imgUpdate(userDTO);
            String msg = result ? "이미지 변경에 성공하였습니다" : "이미지 변경에 실패하였습니다";
            return ResponseEntity.ok(new ApiResponseDTO<>(result,msg,null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"이미지 변경에 실패하였습니다",null));
        }
    }

    //국가리스트조회
    @GetMapping("")
    public ResponseEntity<ApiResponseDTO<List<CountryDTO>>> findCountryList(@RequestHeader(value = "Authorization") String token) {
        try {
            TokenDTO authToken = jwt.validateToken(token);
            if (authToken.getId() != 0 && authToken.getIsStudent() == false) {
                Long id = authToken.getId();
                return ResponseEntity.ok(userService.findCountryList(id));
            }
            else {
                return ResponseEntity.ok(new ApiResponseDTO<>(false,"국가리스트 조회 실패",null));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"국가리스트 조회 실패",null));
        }
    }

    static class Token{
        @JsonProperty
        private final String token;
        public Token(String token){
            this.token = token;
        }
        public String getToken(){
            return this.token;
        }
    }
    static class UserData {
        @JsonProperty
        private final Long id;
        @JsonProperty
        private final String name;
        @JsonProperty
        private final String userId;
        @JsonProperty
        private final String img;
        public UserData(Long id, String name, String userId, String img) {
            this.id = id;
            this.name = name;
            this.userId = userId;
            this.img = img;
        }
    }
    static class UserData2 {
        @JsonProperty
        private final String token;
        private UserData2(String token) {
            this.token = token;
        }
    }
    // 직업id로 직업테이블의 직업을 뽑아와야함
    static class StudentData {
        @JsonProperty
        private final Long id;
        @JsonProperty
        private final String name;
        @JsonProperty
        private final Integer rollNumber;
        @JsonProperty
        private final Integer rating;
        @JsonProperty
        private final String img;
        @JsonProperty
        private final String job;
        @JsonProperty
        private final Long jobId;
        @JsonProperty
        private final Long countryId;
        @JsonProperty
        private final Integer[] skills;
        public StudentData(Long id, String name, Integer rollNumber, Integer rating, String img,String job, Long jobId, Long countryId, Integer[] skills) {
            this.id = id;
            this.name = name;
            this.rollNumber = rollNumber;
            this.rating = rating;
            this.img = img;
            this.job = job;
            this.jobId = jobId;
            this.countryId = countryId;
            this.skills = skills;
        }
    }
}