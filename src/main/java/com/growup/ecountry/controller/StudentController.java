package com.growup.ecountry.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.growup.ecountry.config.TokenProvider;
import com.growup.ecountry.dto.ApiResponseDTO;
import com.growup.ecountry.dto.NoticeDTO;
import com.growup.ecountry.dto.StudentDTO;
import com.growup.ecountry.dto.TokenDTO;
import com.growup.ecountry.entity.Jobs;
import com.growup.ecountry.entity.Notice;
import com.growup.ecountry.repository.JobRepository;
import com.growup.ecountry.repository.NoticeRepository;
import com.growup.ecountry.service.StudentService;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.lang.model.type.NullType;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;
    private final NoticeRepository noticeRepository;
    private final JobRepository jobRepository;
    private final TokenProvider jwt;

    //국민등록(수기)
    @PostMapping("/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> studentAdd(@PathVariable("countryId") Long countryId, @RequestBody List<StudentDTO> studentDTOS){
        return ResponseEntity.ok(studentService.studentAdd(countryId,studentDTOS));
    }
    //국민등록(엑셀)
    @PostMapping("/add/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> studentAddExcel(@PathVariable Long countryId, @RequestParam("file")MultipartFile file) throws IOException {
        return ResponseEntity.ok(studentService.studentAddExcel(countryId,file));
    }

    //국민조회
    @GetMapping("/{countryId}")
    public ResponseEntity<ApiResponseDTO<List<StudentData>>> studentList(@PathVariable("countryId") Long countryId){
        List<StudentData> studentDataList = new ArrayList<>();
        ApiResponseDTO<List<StudentDTO>> apiData = studentService.studentList(countryId,true);
        if(apiData.getResult() != null){
            List<StudentDTO> students = apiData.getResult();
            for(StudentDTO student : students) {
                if(student.getJobId() == null) {
                    StudentData studentData = new StudentData(student.getId(), student.getName(), student.getRollNumber(), student.getRating(),"무직",student.getJobId(),null);
                    studentDataList.add(studentData);
                }
                else {
                    Jobs studentJob = jobRepository.findById(student.getJobId()).get();
                    StudentData studentData = new StudentData(student.getId(), student.getName(), student.getRollNumber(), student.getRating(),studentJob.getName(),student.getJobId(),studentJob.getSkills());
                    studentDataList.add(studentData);
                }
            }
            return ResponseEntity.ok(new ApiResponseDTO<>(apiData.getSuccess(), apiData.getMessage(),studentDataList));
        }
        else {
            return ResponseEntity.ok(new ApiResponseDTO<>(apiData.getSuccess(), apiData.getMessage(),null));
        }
    }
    //국민삭제
    @DeleteMapping("/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> studentDelete(@PathVariable("countryId") Long countryId,@RequestBody StudentDTO studentDTO){
        return ResponseEntity.ok(studentService.studentDelete(countryId,studentDTO.getId()));
    }
    //국민수정
    @PatchMapping("/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> studentUpdate(@PathVariable("countryId") Long countryId,@RequestBody List<StudentDTO> studentDTOs){
        return ResponseEntity.ok(studentService.studentUpdate(countryId,studentDTOs));
    }
    //학생로그인
    @PostMapping("/user/{countryId}")
    public ResponseEntity<ApiResponseDTO<StudentData2>> studentLogin(@PathVariable("countryId") Long countryId,@RequestBody StudentDTO studentDTO){
        ApiResponseDTO<Long> result = studentService.studentLogin(countryId,studentDTO);
        Token token = result.getSuccess() ? new Token(jwt.generateToken(result.getResult(),true)) : new Token(null);
        return ResponseEntity.ok(new ApiResponseDTO<>(result.getSuccess(), result.getMessage(), new StudentData2(token.getToken())));
    }
    //학생비밀번호 변경
    @PatchMapping("/user")
    public ResponseEntity<ApiResponseDTO<NullType>> studentPwUpdate(@RequestHeader("Authorization") String token,@RequestBody StudentDTO studentDTO){
        TokenDTO authToken = jwt.validateToken(token);
        if(authToken != null) {
            return ResponseEntity.ok(studentService.studentPwUpdate(authToken.getId(), studentDTO));
        }
        else {
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"사용자 인증에 실패하였습니다",null));
        }
    }
    //학생이미지 수정
    @PatchMapping("/user/img/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> studentImgUpdate(@PathVariable("countryId") Long countryId,@RequestHeader("Authorization") String token,@RequestBody StudentDTO studentDTO) {
        TokenDTO authToken = jwt.validateToken(token);
        if(authToken != null && authToken.getIsStudent()) {
            return ResponseEntity.ok(studentService.studentImgUpdate(countryId,authToken.getId(),studentDTO.getImg()));
        }
        else {
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"학생만 이용가능 합니다",null));
        }
    }
    //알림조회
    @GetMapping("/notice")
    public ResponseEntity<ApiResponseDTO<List<NoticeData>>> noticeCheck(@RequestHeader("Authorization") String token){
        List<NoticeData> noticeDataList = new ArrayList<>();
        TokenDTO authToken = jwt.validateToken(token);
        if(authToken != null){
            ApiResponseDTO<List<NoticeDTO>> apiData = studentService.noticeList(authToken.getId());
            List<NoticeDTO> notices = apiData.getResult();
            for(NoticeDTO notice : notices) {
                if(notice.getIsChecked() == false){
                    NoticeData noticeData = new NoticeData(notice.getId(), notice.getContent(), 0, notice.getCreatedAt());
                    noticeDataList.add(noticeData);
                    Notice notice1 = noticeRepository.findById(notice.getId()).orElseThrow(()->new IllegalArgumentException("알림이 존재하지 않습니다"));
                    notice1.setIsChecked(true);
                    noticeRepository.save(notice1);
                }
                else { // 이미 조회한걸 다시 보는 경우
                    NoticeData noticeData = new NoticeData(notice.getId(), notice.getContent(), 1, notice.getCreatedAt());
                    noticeDataList.add(noticeData);
                }
            }
            return ResponseEntity.ok(new ApiResponseDTO<>(apiData.getSuccess(), apiData.getMessage(),noticeDataList));
        }
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"사용자 인증에 실패하였습니다",noticeDataList));
    }
    //알림추가(전체 국민한테 한꺼번에 보내는 api도 추가 countryId 받으면 그걸로 studentid 조회해서 메시지로 하나추가
    @PostMapping("/notice/add/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> noticeAdd(@PathVariable("countryId") Long countryId,@RequestBody NoticeDTO noticeDTO){
        return ResponseEntity.ok(studentService.noticeAdd(countryId,noticeDTO));
    }
    @PostMapping("notice/add/all/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> noticeAll(@PathVariable("countryId") Long countryId,@RequestBody NoticeDTO noticeDTO){
        return ResponseEntity.ok(studentService.noticeAddAll(countryId,noticeDTO));
    }
    //알림개수 확인
    @GetMapping("/notice/count")
    public ResponseEntity<ApiResponseDTO<NoticeCount>> noticeCount(@RequestHeader("Authorization") String token){
        try {
            TokenDTO authToken = jwt.validateToken(token);
            if(authToken != null){
                ApiResponseDTO<Integer> apiData = studentService.noticeCount(authToken.getId());
                return ResponseEntity.ok(new ApiResponseDTO<>(apiData.getSuccess(), apiData.getMessage(),new NoticeCount(apiData.getResult())));
            }
            else {
                return ResponseEntity.ok(new ApiResponseDTO<>(false,"사용자 인증에 실패하였습니다",null));
            }
        } catch(Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false,"사용자 인증에 실패하였습니다",null));
        }
    }
    //학생 신용등급 수정
    @PatchMapping("/rating")
    public ResponseEntity<ApiResponseDTO<NullType>> updateRating(@RequestBody StudentDTO studentDTO){
        boolean success = studentService.updateRating(studentDTO);
        String msg = success ? "학생 신용등급 수정에 성공하였습니다." : "학생 신용등급 수정에 실패하였습니다.";
        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success, msg));
    }

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
        private final String job;
        @JsonProperty
        private final Long jobId;
        @JsonProperty
        private final Integer[] skills;
        public StudentData(Long id, String name, Integer rollNumber, Integer rating, String job, Long jobId, Integer[] skills) {
            this.id = id;
            this.name = name;
            this.rollNumber = rollNumber;
            this.rating = rating;
            this.job = job;
            this.jobId = jobId;
            this.skills = skills;
        }
    }
    static class StudentData2 {
        @JsonProperty
        private final String token;
        public StudentData2(String token) {
            this.token = token;
        }
    }
    static class NoticeData {
        @JsonProperty
        private final Long id;
        @JsonProperty
        private final String content;
        @JsonProperty
        private final Integer isChecked;
        @JsonProperty
        private final Date createdAt;
        public NoticeData(Long id, String content, Integer isChecked, Date createdAt) {
            this.id = id;
            this.content = content;
            this.isChecked = isChecked;
            this.createdAt = createdAt;
        }
    }
    static class NoticeCount {
        @JsonProperty
        private final Integer count;
        public NoticeCount(Integer count) {
            this.count = count;
        }
    }
    //토큰 발급
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
}
