package com.growup.ecountry.controller;

import com.growup.ecountry.config.TokenProvider;
import com.growup.ecountry.dto.AccountDTO;
import com.growup.ecountry.dto.ApiResponseDTO;
import com.growup.ecountry.dto.BankDTO;
import com.growup.ecountry.dto.TokenDTO;
import com.growup.ecountry.service.AccountService;
import com.growup.ecountry.service.BankService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.type.NullType;
import java.util.List;

@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class BankController {
    private final AccountService accountService;
    private final BankService bankService;
    private final TokenProvider jwt;

    //전체통장리스트 조회
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<AccountService.AccountInfo>>> getAccount(@RequestHeader(value = "Authorization") String token) {
        try {
            TokenDTO authToken = jwt.validateToken(token);
            if(authToken.getId() == 0L) {
                return ResponseEntity.ok(new ApiResponseDTO<>(false, "로그인 후 이용 가능"));
            }
            if (!authToken.getIsStudent()) {
                return ResponseEntity.ok(new ApiResponseDTO<>(false, "학생만 이용 가능한 서비스"));
            }
            List<AccountService.AccountInfo> result = accountService.getAccount(authToken.getId());
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "통장 조회 완료", result));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false, "통장 조회 실패"));
        }
    }
    //통장 상세정보 조회
    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponseDTO<AccountService.AccountInfo>> getAccountDetail(@RequestHeader(value = "Authorization") String token, @PathVariable Long accountId){
        try {
            TokenDTO authToken = jwt.validateToken(token);
            if(authToken.getId() == 0L) {
                return ResponseEntity.ok(new ApiResponseDTO<>(false, "로그인 후 이용 가능"));
            }
            if (!authToken.getIsStudent()) {
                return ResponseEntity.ok(new ApiResponseDTO<>(false, "학생만 이용 가능한 서비스"));
            }
            AccountService.AccountInfo result = accountService.getAccountDetail(accountId);
            if(result != null){
                return ResponseEntity.ok(new ApiResponseDTO<>(true, "통장 조회 완료", result));
            }else {
                return ResponseEntity.ok(new ApiResponseDTO<>(false, "통장 조회 실패"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false, "통장 조회 실패"));
        }
    }
    @PostMapping
    public ResponseEntity<ApiResponseDTO<BankDTO>> transaction(@RequestBody BankDTO bankDTO) {
        try {
            BankDTO result = bankService.createBank(bankDTO);
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "송금 완료", result));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.ok(new ApiResponseDTO<>(false, "송금 실패"));
        }
    }
    @GetMapping("/list/{accountId}")
    public ResponseEntity<ApiResponseDTO<List<BankDTO>>> getHistory(@PathVariable("countryId") Long accountId) {
        try {
            System.out.println("accountId : "+accountId);
            List<BankDTO> result = bankService.getBank(accountId);
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "거래 내역 조회 완료", result));
        } catch (Exception e) {
            System.out.println("거래내역조회실패 : " + e +  e.getMessage());
            return ResponseEntity.ok(new ApiResponseDTO<>(false, "거래 내역 조회 실패"));
        }
    }
    //입금 가능 리스트
    @GetMapping("/students/{countryId}")
    public ResponseEntity<ApiResponseDTO<List<AccountDTO>>> getStudent(@PathVariable("countryId") Long countryId) {
        try {
            List<AccountDTO> result = bankService.getBankList(countryId);
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "입출금 통장 조회 완료", result));
        } catch (Exception e) {
            System.out.println("입금 가능 리스트 조회실패 : " + e +  e.getMessage());
            return ResponseEntity.ok(new ApiResponseDTO<>(false, "입출금 통장 조회 실패"));
        }
    }
    //월급명세서
    @GetMapping("/paystub")
    public ResponseEntity<ApiResponseDTO<List<BankService.PaystubDTO>>> getPaystub(@RequestHeader(value = "Authorization") String token) {
        try {
            TokenDTO authToken = jwt.validateToken(token);
            if(authToken.getId() != 0) {
                 return ResponseEntity.ok(bankService.getPaystub(authToken.getId()));
            }
            else {
                return ResponseEntity.ok(new ApiResponseDTO<>(false, "사용자 인증 실패", null));
            }
        }
        catch (Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false, "월급명세서 조회 실패", null));
        }
    }
    //월급금액확인
    @GetMapping("/salary")
    public ResponseEntity<ApiResponseDTO<SalaryData>> getSalary(@RequestParam(name = "studentId",required = false) Long studentId) {
        try {
            ApiResponseDTO<Integer> apiData = bankService.getSalary(studentId);
            return ResponseEntity.ok(new ApiResponseDTO<>(apiData.getSuccess(), apiData.getMessage(), new SalaryData(apiData.getResult())));
        } catch(Exception e) {
            return ResponseEntity.ok(new ApiResponseDTO<>(false, e.getMessage(), null));
        }
    }
    //국가 화폐단위 조회
    @GetMapping("/unit/{countryId}")
    public ResponseEntity<ApiResponseDTO<JSONObject>> getUnit(@PathVariable("countryId") Long countryId) {
        JSONObject responseData = new JSONObject();
        try {
            responseData.put("unit",  bankService.getUnit(countryId));
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "화폐단위 조회에 성공하였습니다.",responseData));
        }catch (Exception e){
            return ResponseEntity.ok(new ApiResponseDTO<>(false, "화폐단위 조회에 실패하였습니다."));
        }
    }


    static class SalaryData {
        private final Integer value;
        public SalaryData (Integer value) {
            this.value = value;
        }
        //직렬화오류로 추가
        public Integer getValue(){
            return this.value;
        }
    }
}
