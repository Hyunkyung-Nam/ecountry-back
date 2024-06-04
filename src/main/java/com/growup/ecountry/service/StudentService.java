package com.growup.ecountry.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.growup.ecountry.dto.*;
import com.growup.ecountry.entity.*;
import com.growup.ecountry.repository.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.Count;
import org.aspectj.weaver.ast.Not;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.lang.model.type.NullType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final RestTemplate restTemplate;
    private final StudentRepository studentRepository;
    private final CountryRepository countryRepository;
    private final AccountRepository accountRepository;
    private final AccountListRepository accountListRepository;
    private final NoticeRepository noticeRepository;
    private final JobRepository jobRepository;
    //국민등록(수기)
    public ApiResponseDTO<NullType> studentAdd(Long countryId, List<StudentDTO> students){
        Optional<Countries> countryExist = countryRepository.findById(countryId);
        if(countryExist.isPresent()){
            Countries countries = countryExist.get();
            if(countries.getAvailable()){
                for(StudentDTO student : students){
                    Students studentEntity = Students.builder()
                            .name(student.getName())
                            .rollNumber(student.getRollNumber())
                            .pw(student.getPw())
                            .img(student.getImg())
                            .countryId(countries.getId())
                            .jobId(null).build();
                    studentRepository.save(studentEntity);
                    List<AccountLists> accountInfo = accountListRepository.findByCountryIdAndDivisionAndAvailable(countryId, false, true);
                    if(!accountInfo.isEmpty()){
                        Accounts accounts = Accounts.builder()
                                .balance(0).accountListId(accountInfo.get(0).getId())
                                .studentId(studentEntity.getId()).build();
                        accountRepository.save(accounts);
                    }
                    else {
                        return new ApiResponseDTO<>(false,"사용 가능한 계좌 목록이 없습니다",null);
                    }
                }
                return new ApiResponseDTO<>(true,"국민등록 성공",null);
            }
            else {
                return new ApiResponseDTO<>(false,"비활성화된 국가 입니다",null);
            }
        }
        else {
            return new ApiResponseDTO<>(false,"국가가 존재하지 않습니다",null);
        }
    }
    //국민등록(엑셀)
    public ApiResponseDTO<NullType> studentAddExcel(Long countryId, MultipartFile file) throws IOException {
        Optional<Countries> countryExist = countryRepository.findById(countryId);
        if(countryExist.isPresent()){
            Countries countries = countryExist.get();
            if(countries.getAvailable()){
                String fileName = file.getOriginalFilename();
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                if (!extension.equals("csv")) {
                    throw new IOException("CSV파일만 업로드 해주세요.");
                }
                try (InputStream inputStream = file.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    reader.readLine();

                    while ((line = reader.readLine()) != null) {
                        String[] columns = line.split(",");
                        //columns[0]: roll number, columns[1]: name,columns[2]: password
                        Students student = Students.builder()
                                .rollNumber(Integer.parseInt(columns[0]))
                                .name(columns[1])
                                .pw(columns[2])
                                .countryId(countries.getId())
                                .jobId(null).build();
                        studentRepository.save(student);
                        List<AccountLists> accountInfo = accountListRepository.findByCountryIdAndDivisionAndAvailable(countryId, false, true);
                        if(!accountInfo.isEmpty()){
                            Accounts accounts = Accounts.builder()
                                    .balance(0).accountListId(accountInfo.get(0).getId())
                                    .studentId(student.getId()).build();
                            accountRepository.save(accounts);
                        }
                        else {
                            return new ApiResponseDTO<>(false,"사용 가능한 계좌 목록이 없습니다",null);
                        }
                        Accounts accounts = Accounts.builder()
                                .balance(0)
                                .studentId(student.getId()).build();
                        accountRepository.save(accounts);
                    }
                    return new ApiResponseDTO<>(true,"국민등록 성공",null);
                }
            }
            else {
                return new ApiResponseDTO<>(false,"비활성화된 국가 입니다",null);
            }
        }
        else {
            return new ApiResponseDTO<>(false,"국가가 존재하지 않습니다",null);
        }
    }
    //국민조회
    public ApiResponseDTO<List<StudentDTO>> studentList(Long countryId,Boolean available){
        Countries countries = countryRepository.findById(countryId).orElseThrow(()->{ throw new IllegalArgumentException("존재하지 않는 국가입니다");});
            if(countries.getAvailable()){
                List<Students> students = studentRepository.findAllByCountryIdAndAvailable(countryId, available);
                List<StudentDTO> studentDTOList = new ArrayList<>();
                for(Students student : students){
                    StudentDTO studentDTO = StudentDTO.builder()
                            .id(student.getId())
                            .name(student.getName())
                            .rollNumber(student.getRollNumber())
                            .rating(student.getRating())
                            .jobId(student.getJobId())
                            .build();
                    studentDTOList.add(studentDTO);
                }
                return new ApiResponseDTO<>(true,"국민조회 성공",studentDTOList);
            }
            else {
                return new ApiResponseDTO<>(false,"비활성화된 국가 입니다",null);
            }
    }
    //국민삭제
    public ApiResponseDTO<NullType> studentDelete(Long countryId,Long id){
        Countries countries = countryRepository.findById(countryId).orElseThrow(()->{ throw new IllegalArgumentException("존재하지 않는 국가입니다");});
        if(countries.getAvailable()){
            Optional<Students> studentExist = studentRepository.findByIdANDCountryId(id,countryId);
            if(studentExist.isPresent()){
                Students student = studentExist.get();
                student.setAvailable(false);
                studentRepository.save(student);
                return new ApiResponseDTO<>(true,"국민삭제 성공",null);
            }
            else {
                return new ApiResponseDTO<>(false,"국민이 존재하지 않습니다",null);
            }
        }
        else {
            return new ApiResponseDTO<>(false,"비활성화된 국가 입니다",null);
        }
    }
    //국민수정
    public ApiResponseDTO<NullType> studentUpdate(Long countryId,List<StudentDTO> studentDTOs){
        Countries countries = countryRepository.findById(countryId).orElseThrow(()->{ throw new IllegalArgumentException("국가가 존재하지 않습니다");});
        if(countries.getAvailable()){
            for(StudentDTO studentDTO : studentDTOs){
                Students student = studentRepository.findByIdANDCountryId(studentDTO.getId(),countryId).orElseThrow(()->{
                    throw new IllegalArgumentException("학생 정보 혹은 국가 아이디가 존재하지 않습니다");
                });
                if(studentDTO.getPw() != null){
                    student = Students.builder()
                            .id(studentDTO.getId())
                            .name(studentDTO.getName())
                            .rollNumber(studentDTO.getRollNumber())
                            .pw(studentDTO.getPw())
                            .available(student.getAvailable())
                            .rating(studentDTO.getRating())
                            .jobId(studentDTO.getJobId())
                            .img(studentDTO.getImg())
                            .countryId(student.getCountryId()).build();
                    studentRepository.save(student);
                }
                else {
                    student = Students.builder()
                            .id(studentDTO.getId())
                            .name(studentDTO.getName())
                            .rollNumber(studentDTO.getRollNumber())
                            .pw(student.getPw())
                            .available(student.getAvailable())
                            .rating(studentDTO.getRating())
                            .jobId(studentDTO.getJobId())
                            .countryId(student.getCountryId()).build();
                    studentRepository.save(student);
                }
            }
            return new ApiResponseDTO<>(true,"국민수정 성공",null);
        }
        else {
            return new ApiResponseDTO<>(false,"비활성화된 국가 입니다",null);
        }
    }
    //학생로그인
    public ApiResponseDTO<Long> studentLogin(Long countryId,StudentDTO studentDTO){
            Optional<Countries> countryExist = countryRepository.findById(countryId);
            if(countryExist.isPresent()){
                Countries countries = countryExist.get();
                if(countries.getAvailable()){
                    Optional<Students> studentExist = studentRepository.findByNameANDPwANDRollNumberANDCountryId(studentDTO.getName(),studentDTO.getPw(),studentDTO.getRollNumber(), countries.getId());
                    if(studentExist.isPresent()){
                        Students students = studentExist.get();
                        if(students.getAvailable()){
                            return new ApiResponseDTO<>(true,"학생 로그인 성공",students.getId());
                        }
                        else {
                            return new ApiResponseDTO<>(false,"비활성화된 계정입니다.",null);
                        }
                    }
                    else {
                        return new ApiResponseDTO<>(false,"사용자 정보가 일치하지 않습니다",null);
                    }
                }
                else {
                    return new ApiResponseDTO<>(false,"비활성화된 국가 입니다",null);
                }
            }
            else {
                return new ApiResponseDTO<>(false,"국가가 존재하지 않습니다",null);
            }
    }
    //학생비밀번호 변경
    public ApiResponseDTO<NullType> studentPwUpdate(Long id,StudentDTO studentDTO){
        Optional<Students> studentExist = studentRepository.findById(id);
        if(studentExist.isPresent()){
            Students student = studentExist.get();
            student = Students.builder()
                    .id(student.getId())
                    .name(student.getName())
                    .rollNumber(student.getRollNumber())
                    .pw(studentDTO.getPw())
                    .rating(student.getRating())
                    .available(student.getAvailable())
                    .countryId(student.getCountryId()).build();
            studentRepository.save(student);
            return new ApiResponseDTO<>(true,"비밀번호 변경 성공",null);
        }
        else {
            return new ApiResponseDTO<>(false,"국민이 존재하지 않습니다",null);
        }
    }
    //학생이미지 수정
    public ApiResponseDTO<NullType> studentImgUpdate(Long countryId,Long studentId, String img) {
        String API_URL = "https://api.kakaobrain.com/v2/inference/karlo/t2i";

        Optional<Students> studentExist = studentRepository.findByIdANDCountryId(studentId,countryId);
        if(studentExist.isPresent()){
            Students student = studentExist.get();
                student = Students.builder()
                        .id(student.getId())
                        .name(student.getName())
                        .rollNumber(student.getRollNumber())
                        .pw(student.getPw())
                        .rating(student.getRating())
                        .available(student.getAvailable())
                        .countryId(student.getCountryId())
                        .jobId(student.getJobId())
                        .img(img).build();
                //유직
                if(student.getJobId() != null){
                    Jobs studentJob = jobRepository.findById(student.getJobId()).get();
                    Map<String, Object> requestData = new HashMap<>();
                    requestData.put("version", "v2.1");
                    requestData.put("prompt", "A photorealistic image of job as " + studentJob.getName() + "that looks modern and professional.");
                    requestData.put("height", 1024);
                    requestData.put("width", 1024);

                    HttpHeaders headers = new HttpHeaders();
                    String rest_api_key = System.getenv("KAKAO_REST_API_KEY");
                    headers.set("Authorization", "KakaoAK " + rest_api_key);
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);
                    ResponseEntity<KakaoResponse> responseEntity = restTemplate.postForEntity(API_URL, requestEntity, KakaoResponse.class);
                    String imgUrl = null;
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        imgUrl = responseEntity.getBody().getImages().get(0).getImage();
                        student.setJobImg(imgUrl);
                        studentRepository.save(student);
                        return new ApiResponseDTO<>(true,"이미지 + 직업 이미지 변경 성공",null);
                    } else {
                        // 이부분은 카카오 api 오류 발생 시 예외처리
                        student.setJobImg(null);
                        studentRepository.save(student);
                        return new ApiResponseDTO<>(true,"이미지 변경 성공",null);
                    }
                } // 무직
                else {
                    Map<String, Object> requestData = new HashMap<>();
                    requestData.put("version", "v2.1");
                    requestData.put("prompt", "A photorealistic image of school interior that looks modern and school-like.");
                    requestData.put("negative_prompt", "human");
                    requestData.put("height", 1024);
                    requestData.put("width", 1024);

                    HttpHeaders headers = new HttpHeaders();
                    String rest_api_key = System.getenv("KAKAO_REST_API_KEY");
                    headers.set("Authorization", "KakaoAK " + rest_api_key);
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);
                    ResponseEntity<KakaoResponse> responseEntity = restTemplate.postForEntity(API_URL, requestEntity, KakaoResponse.class);
                    String imgUrl = null;
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        imgUrl = responseEntity.getBody().getImages().get(0).getImage();
                        student.setJobImg(imgUrl);
                        studentRepository.save(student);
                        return new ApiResponseDTO<>(true,"이미지 + 직업 이미지 변경 성공",null);
                    } else {
                        // 이부분은 카카오 api 오류 발생 시 예외처리
                        student.setJobImg(imgUrl);
                        studentRepository.save(student);
                        return new ApiResponseDTO<>(true,"이미지 변경 성공",null);
                    }
                }
        }
        else {
            return new ApiResponseDTO<>(false,"국민이 존재하지 않습니다",null);
        }
    }
    //알림조회
    public ApiResponseDTO<List<NoticeDTO>> noticeList(Long studentId){
        List<NoticeDTO> noticeDTOList = new ArrayList<>();
        Optional<Students> studentExist = studentRepository.findById(studentId);
        if(studentExist.isPresent()){
            Students student = studentExist.get();
            List<Notice> noticeList = noticeRepository.findAllByStudentId(student.getId());
            for(Notice notice : noticeList){
                NoticeDTO noticeDTO = NoticeDTO.builder()
                        .id(notice.getId())
                        .content(notice.getContent())
                        .isChecked(notice.getIsChecked())
                        .createdAt(notice.getCreatedAt())
                        .studentId(List.of(notice.getStudentId()))
                        .build();
                noticeDTOList.add(noticeDTO);
                //알림 확인으로 update
                Notice updateNotice = Notice.builder()
                        .id(notice.getId())
                        .content(notice.getContent())
                        .isChecked(true)
                        .createdAt(notice.getCreatedAt())
                        .studentId(notice.getStudentId())
                        .build();
                noticeRepository.save(updateNotice);
            }
            return new ApiResponseDTO<>(true,"알림조회 성공",noticeDTOList);
        }
        else {
            return new ApiResponseDTO<>(false,"알림 조회 실패: 국민이 존재하지 않습니다",null);
        }
    }
    //알림추가
    public ApiResponseDTO<NullType> noticeAdd(Long countryId,NoticeDTO noticeDTO){
        Optional<Countries> countryExist = countryRepository.findById(countryId);
        if(countryExist.isPresent()){
            for(Long studentId : noticeDTO.getStudentId()){
                Optional<Students> studentExist = studentRepository.findById(studentId);
                if (studentExist.isPresent()) {
                    Students student = studentExist.get();
                    Notice notice = Notice.builder()
                            .content(noticeDTO.getContent())
                            .studentId(student.getId())
                            .build();
                    noticeRepository.save(notice);
                }
            }
            return new ApiResponseDTO<>(true,"알림이 발송되었습니다",null);
        }
        else {
            return new ApiResponseDTO<>(false,"알림 발송에 실패하였습니다",null);
        }
    }
    //국민 전체에게 알림 추가
    public ApiResponseDTO<NullType> noticeAddAll(Long countryId,NoticeDTO noticeDTO){
        List<Students> studentList = studentRepository.findAllByCountryId(countryId);
        for(Students student : studentList){
            if(student.getAvailable()){
                Notice notice = Notice.builder()
                        .content(noticeDTO.getContent())
                        .studentId(student.getId())
                        .build();
                noticeRepository.save(notice);
            }
        }
        return new ApiResponseDTO<>(true,"국민 전체에게 알림 추가 성공",null);
    }
    //알림개수 확인
    public ApiResponseDTO<Integer> noticeCount(Long studentId){
        Students student = studentRepository.findById(studentId).orElseThrow(()->new IllegalArgumentException("학생이 존재하지 않습니다"));
        List<Notice> notices = noticeRepository.findAllByStudentId(student.getId());
        Integer count = 0;
        for(Notice notice : notices){
            if(notice.getIsChecked() == false){
                count += 1;
            }
        }
        return new ApiResponseDTO<>(true,"알림 개수",count);
    }
    //학생 신용등급 수정
    public boolean updateRating(StudentDTO studentDTO){
        Students student = studentRepository.findById(studentDTO.getId()).orElseThrow();
        try {
            student.setRating(studentDTO.getRating());
            studentRepository.save(student);
        }catch (Exception e){
            System.out.print("학생 신용등급 수정 오류 : " + e.getMessage());
            return false;
        }
        return true;
    }
//    학생 국가 조회
    public List<Long> findStudent(Long id) {
        Optional<Students> students = studentRepository.findById(id);
        List<Long> countryList = new ArrayList<>();
        students.ifPresent(value -> countryList.add(value.getCountryId()));
        return countryList;
    }
}

