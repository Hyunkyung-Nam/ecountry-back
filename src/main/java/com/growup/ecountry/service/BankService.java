package com.growup.ecountry.service;

import com.growup.ecountry.dto.AccountDTO;
import com.growup.ecountry.dto.ApiResponseDTO;
import com.growup.ecountry.dto.BankDTO;
import com.growup.ecountry.dto.CountryDTO;
import com.growup.ecountry.entity.*;
import com.growup.ecountry.repository.*;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.lang.model.type.NullType;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankService {
    private final AccountRepository accountRepository;
    private final BankRepository bankRepository;
    private final StudentRepository studentRepository;
    private final AccountListRepository accountListRepository;
    private final CountryRepository countryRepository;
    private final TaxRepository taxRepository;
    private final JobRepository jobRepository;

    public String getStudentName(@Nullable Long accountId) {
        try {
            if(Objects.nonNull(accountId) && accountId == 0){
                return "급여";
            }
            System.out.println("accountId : " + accountId);
            Long studentId = accountRepository.findById(accountId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID 입니다.")).getStudentId();
            return studentRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID 입니다.")).getName();
        }catch (Exception e){
            System.out.println("이름가져오기 오류 accountId는 " +accountId + e.getMessage() );
            return null;
        }

    }

    public Integer getStudentRollNumber(Long accountId) {
        Long studentId = accountRepository.findById(accountId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID 입니다.")).getStudentId();
        return studentRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID 입니다.")).getRollNumber();
    }


    public BankDTO createBank(BankDTO bankDTO) {
        System.out.println(getStudentName(bankDTO.getDepositId()));

        Banks result = bankRepository.save(Banks.builder().transaction(bankDTO.getTransaction())
                .memo(bankDTO.getMemo()).depositId(bankDTO.getDepositId())
                .withdrawId(bankDTO.getWithdrawId()).isPenalty(0L).build());

        Accounts deposit = accountRepository.findById(bankDTO.getDepositId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID 입니다."));
        deposit.setBalance(deposit.getBalance()+bankDTO.getTransaction());
        accountRepository.save(deposit);

        //월급지급 제외한 경우만 저장
        if(bankDTO.getWithdrawId() != 0){
            Accounts withdraw = accountRepository.findById(bankDTO.getWithdrawId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ID 입니다."));
            withdraw.setBalance(withdraw.getBalance()-bankDTO.getTransaction());
            accountRepository.save(withdraw);
        }

        return BankDTO.builder().id(result.getId()).transaction(result.getTransaction())
                .createdAt(result.getCreatedAt()).memo(result.getMemo()).depositId(result.getDepositId())
                .withdrawId(result.getWithdrawId()).depositName(getStudentName(result.getDepositId())).build();
    }

    //입출급내역조회
    public List<BankDTO> getBank(Long accountId) {

        return bankRepository.findByDepositIdOrWithdrawIdOrderByIdDesc(accountId, accountId).stream().map(list ->
                BankDTO.builder()
                .id(list.getId()).transaction(list.getTransaction()).createdAt(list.getCreatedAt())
                .memo(list.getMemo()).isPenalty(list.getIsPenalty()).depositId(list.getDepositId()).withdrawId(list.getWithdrawId())
                .depositName(getStudentName(list.getDepositId())).withdrawName(getStudentName(list.getWithdrawId())).build()).collect(Collectors.toList());
    }
    // 입금 가능 리스트
    public List<AccountDTO> getBankList(Long countryId) {
        Long accountListId = accountListRepository.findByCountryIdAndDivisionAndAvailable(countryId, false, true).get(0).getId();
        List<Accounts> accountsList=  accountRepository.findByAccountListId(accountListId);
        List<AccountDTO> accountDTOList = new ArrayList<>();
        for(Accounts account :accountsList){
            boolean isAvailable = studentRepository.findById(account.getStudentId()).orElseThrow().getAvailable();
            if (isAvailable) {
                accountDTOList.add(AccountDTO.builder()
                        .id(account.getId()).name(getStudentName(account.getId())).studentId(account.getStudentId()).rollNumber(getStudentRollNumber(account.getId())).build());
            }
        }

        Collections.sort(accountDTOList, new Comparator<AccountDTO>() {
            @Override
            public int compare(AccountDTO o1, AccountDTO o2) {
                //학급번호로 오름차순 정렬
                if(o1.getRollNumber() >= o2.getRollNumber()){
                    return 1;
                }
                return -1;
            }
        });
        return accountDTOList;
    }
    //월급명세서
    public ApiResponseDTO<List<PaystubDTO>> getPaystub(Long studentId) {
        Students student = studentRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));
        if(student.getJobId() == null) {
            //테스트를 위해 일단 적어놓고 테스트 끝나면 다시 지워야함
            Integer salary = 0; // 월급
            List<Taxes> taxes = taxRepository.findByCountryId(student.getCountryId());
            List<PaystubDTO> paystubDTOList = new ArrayList<>();
            PaystubDTO salaryDTO = PaystubDTO.builder()
                    .title("월급")
                    .value(Integer.valueOf((int)Math.floor(salary)))
                    .build();
            paystubDTOList.add(salaryDTO);
            for(Taxes tax : taxes) {
                if(tax.getDivision() == 0) {
                    PaystubDTO paystubDTO = PaystubDTO.builder()
                            .title(tax.getName())
                            .value(Integer.valueOf((int)Math.round(-(salary * tax.getTax() / 100)))) // % 비율
                            .build();
                    paystubDTOList.add(paystubDTO);
                }
                else if(tax.getDivision() == 3) {
                }
                else {
                    PaystubDTO paystubDTO = PaystubDTO.builder()
                            .title(tax.getName())
                            .value(Integer.valueOf((int)Math.floor(-(tax.getTax())))) // 그외 나머지 고정금액
                            .build();
                    paystubDTOList.add(paystubDTO);
                }
            }
            return new ApiResponseDTO<>(true, "무직의 월급명세서", paystubDTOList);
        }
        // + - 다 더해서 원금 뜰수있게
        Jobs jobs = jobRepository.findById(student.getJobId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직업입니다"));
        Integer salary = jobs.getSalary(); //월급
        List<Taxes> taxes = taxRepository.findByCountryId(student.getCountryId());
        List<PaystubDTO> paystubDTOList = new ArrayList<>();
        PaystubDTO salaryDTO = PaystubDTO.builder()
                .title("월급")
                .value(Integer.valueOf((int)Math.floor(salary)))
                .build();
        paystubDTOList.add(salaryDTO);
        for(Taxes tax : taxes) {
            if(tax.getDivision() == 0) {
                PaystubDTO paystubDTO = PaystubDTO.builder()
                        .title(tax.getName())
                        .value(Integer.valueOf((int)Math.round(-(salary * tax.getTax() / 100)))) // % 비율
                        .build();
                paystubDTOList.add(paystubDTO);
            }
            else if(tax.getDivision() == 3) {
            }
            else {
                PaystubDTO paystubDTO = PaystubDTO.builder()
                        .title(tax.getName())
                        .value(Integer.valueOf((int)Math.floor(-(tax.getTax())))) // 그외 나머지 고정금액
                        .build();
                paystubDTOList.add(paystubDTO);
            }

        }
        return new ApiResponseDTO<>(true, "월급명세서", paystubDTOList);
    }
    //월급금액확인:실수령액
    public ApiResponseDTO<Integer> getSalary(Long id) {
        Students student = studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));
        //무직월급
        if(student.getJobId() == null) {
            Integer actualSalary = 0;
            List<Taxes> taxes = taxRepository.findByCountryId(student.getCountryId());
            for(Taxes tax : taxes) {
                if(tax.getDivision() == 0) {
                    actualSalary = Integer.valueOf((int)Math.round(actualSalary - (actualSalary * tax.getTax() / 100)));
                }
                else {
                    actualSalary = Integer.valueOf((int)Math.floor(actualSalary - tax.getTax()));
                }
            }
            return new ApiResponseDTO<>(true, "월급금액 확인", actualSalary);
        }
        else {
            Jobs jobs = jobRepository.findById(student.getJobId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직업입니다"));
            Integer actualSalary = jobs.getSalary(); // 월급
            List<Taxes> taxes = taxRepository.findByCountryId(student.getCountryId());
            for(Taxes tax : taxes) {
                if(tax.getDivision() == 0) {
                    actualSalary = Integer.valueOf((int)Math.round(actualSalary - (actualSalary * tax.getTax() / 100)));
                }
                else {
                    actualSalary = Integer.valueOf((int)Math.floor(actualSalary - tax.getTax()));
                }
            }
            return new ApiResponseDTO<>(true, "월급금액 확인", actualSalary);
        }
    }
    //국가 화폐단위 조회
    public String getUnit(Long countryId){
        return countryRepository.findById(countryId).orElseThrow().getUnit();
    }
    @Getter
    @Setter
    @Builder
    public static class PaystubDTO {
        private String title;
        private Integer value;
        public PaystubDTO(String title,Integer value) {
            this.title = title;
            this.value = value;
        }
    }
}














//        List<Accounts> accounts = accountRepository.findByStudentId(student.getId());
//        // Bank.id 로 조회하기
//        Banks banks = Banks.builder()
//                .transaction(jobs.getSalary())
//                .memo("월급")
//                .depositId(accounts.get(0).getId()) // 학생 계좌
//                .withdrawId(0L) // 0 : 월급 통장
//                .build();
//        bankRepository.save(banks);
//        return new ApiResponseDTO<>(true, "월급금액 확인", banks);