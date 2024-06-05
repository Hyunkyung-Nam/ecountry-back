package com.growup.ecountry.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.growup.ecountry.dto.*;
import com.growup.ecountry.service.CountryService;
import com.growup.ecountry.service.TaxService;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.type.NullType;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tax")
@RequiredArgsConstructor
public class TaxController {
    public  final TaxService taxService;
    public final CountryService countryService;

    //세금리스트 등록
    @PostMapping()
    public ResponseEntity<ApiResponseDTO<NullType>> createTaxes(@RequestBody List<TaxDTO> taxDTOList){
        Boolean success = taxService.addTaxes(taxDTOList);
        String msg = success ? "세금리스트 추가에 성공하였습니다." : "세금리스트 추가에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success,msg));
    }
    //세금리스트 조회
    @GetMapping("/{countryId}")
    public ResponseEntity<ApiResponseDTO<List<FindAllTaxes>>> findAllTaxes(@PathVariable("countryId") Long countryId){
        List<TaxDTO> taxDTOList = taxService.findAllTaxes(countryId);
        String msg = taxDTOList != null ? "세금리스트 조회에 성공하였습니다." : "세금리스트 조회에 실패하였습니다.";
        boolean success = false;
        System.out.println("length : " + taxDTOList.size());
        List<FindAllTaxes> findAllTaxesList = new ArrayList<>();
        try {
            taxDTOList.forEach(taxDTO -> findAllTaxesList.add(
                    new FindAllTaxes(taxDTO.getId(), taxDTO.getName(), taxDTO.getDivision(), taxDTO.getTax()))
            );
            success = true;
        }catch (Exception e){
            msg = "세금리스트 조회에 실패하였습니다.";
        }
        return ResponseEntity.ok(new ApiResponseDTO<List<FindAllTaxes>>(success, msg, findAllTaxesList));
    }

    //세금리스트 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<NullType>> deleteTax(@PathVariable Long id){
        Boolean success = taxService.deleteTax(id);
        String msg = success ? "세금리스트 1개 삭제에 성공하였습니다." : "세금리스트 1개 삭제에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success, msg));
    }
    //세금리스트 수정
    @PatchMapping
    public ResponseEntity<ApiResponseDTO<NullType>> updateTax(@RequestBody TaxDTO taxDTO){
        Boolean success = taxService.updateTax(taxDTO);
        String msg = success ? "세금리스트 1개 수정에 성공하였습니다." : "세금리스트 1개 수정에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success, msg));
    }
    //국고 조회
    @GetMapping("/treasury/{countryId}")
    public ResponseEntity<ApiResponseDTO<?>> findTreasury(@PathVariable("countryId") Long countryId){
        CountryDTO countryDTO = countryService.findTreasury(countryId);
        if(countryDTO == null){
            return ResponseEntity.ok(new ApiResponseDTO<NullType>(false, "국고 조회에 실패하였습니다."));
        }else {
            Treasury responseTreasury = new Treasury(countryDTO.getTreasury());
            return ResponseEntity.ok(new ApiResponseDTO<Treasury>(true, "국고 조회에 성공하였습니다.",responseTreasury));
        }
    }
    //과태료 조회
    @GetMapping("/penalty/{countryId}")
    public ResponseEntity<ApiResponseDTO<List<Penalty>>> findAllPenaltyList(@PathVariable("countryId") Long countryId){
        try {
            List<Penalty> penalties = taxService.findPenalty(countryId).stream().map(penalty -> Penalty.builder().id(penalty.getId()).withdrawId(penalty.getWithdrawId()).memo(penalty.getMemo()).createdAt(penalty.getCreatedAt()).transaction(penalty.getTransaction()).build()).toList();
            return ResponseEntity.ok(new ApiResponseDTO<List<Penalty>>(true, "과태료 조회에 성공하였습니다.", penalties));

        }catch (Exception e){
            System.out.println("과태표 조회 controller 오류 : " + e.getMessage());
            return ResponseEntity.ok(new ApiResponseDTO<List<Penalty>>(false, "과태료 조회에 실패하였습니다.", null));
        }
    }
    //과태료 리스트 조회
    @GetMapping("/penalty/list/{countryId}")
    public ResponseEntity<ApiResponseDTO<List<TaxService.PenaltyList>>> findPenaltyList(@PathVariable("countryId") Long countryId){
            List<TaxService.PenaltyList> penaltyList = taxService.findPenaltyList(countryId);
            if(penaltyList != null) {
                return ResponseEntity.ok(new ApiResponseDTO<List<TaxService.PenaltyList>>(true, "과태료 리스트조회에 성공하였습니다.", penaltyList));
            } else {
                return ResponseEntity.ok(new ApiResponseDTO<List<TaxService.PenaltyList>>(false, "과태료 리스트조회에 실패하였습니다."));
            }
    }

    //과태료 부과
    @PostMapping("/penalty/{countryId}")
    public ResponseEntity<ApiResponseDTO<NullType>> imposePenalty(@PathVariable("countryId") Long countryId, @RequestBody BankDTO bankDTO){
        boolean success = taxService.imposePenalty(countryId, bankDTO);
        String msg = success ? "과태료 부과에 성공하였습니다." : "과태료 부과에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success, msg));
    }

    //과태료 삭제
    @DeleteMapping("/penalty/{id}")
    public ResponseEntity<ApiResponseDTO<NullType>> deletePenalty(@PathVariable("id") Long id){
        boolean success = taxService.deletePenalty(id);
        String msg = success ? "과태료 삭제에 성공하였습니다." : "과태료 삭제에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success, msg));
    }

    @Builder
    static class Penalty{
        @JsonProperty
        private Long id;
        @JsonProperty
        private Integer transaction;
        @JsonProperty
        private String memo ;
        @JsonProperty
        private Timestamp createdAt;
        @JsonProperty
        private Long withdrawId;

        public Penalty(Long id, Integer transaction, String memo, Timestamp createdAt, Long withdrawId) {
            this.id = id;
            this.transaction = transaction;
            this.memo = memo;
            this.createdAt = createdAt;
            this.withdrawId = withdrawId;
        }
    }


    static class Treasury{
        @JsonProperty
        private Integer treasury;
        public Treasury(Integer treasury){
            this.treasury = treasury;
        }
    }
    static class FindAllTaxes{
        @JsonProperty
        private Long id;
        @JsonProperty
        private String name;
        @JsonProperty
        private Integer division;
        @JsonProperty
        private Double tax;

        public FindAllTaxes(Long id, String name, Integer division, Double tax){
            this.id = id;
            this.name = name;
            this.division = division;
            this.tax = tax;
        }
    }
}
