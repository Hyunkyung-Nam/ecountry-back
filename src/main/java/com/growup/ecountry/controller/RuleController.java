package com.growup.ecountry.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.growup.ecountry.dto.ApiResponseDTO;
import com.growup.ecountry.dto.RuleDTO;
import com.growup.ecountry.service.RuleService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.type.NullType;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rule")
@RequiredArgsConstructor
public class RuleController {
    public final RuleService ruleService;

    //규칙리스트 등록
    @PostMapping
    public ResponseEntity<ApiResponseDTO<NullType>> createRules(@RequestBody List<RuleDTO> ruleDTOList){
        Boolean result = ruleService.setRules(ruleDTOList);
        String msg = result ? "규칙생성에 성공하였습니다." : "규칙생성에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NullType>(result, msg));
    }
    //규칙리스트 조회
    @GetMapping("/{countryId}")
    public ResponseEntity<FindAllRuleResponse> findAllRules(@PathVariable("countryId") Long countryId){
        List<RuleDTO> ruleDTOList = ruleService.findAllRules(countryId);
        String msg = ruleDTOList != null ? "규칙조회에 성공하였습니다." : "규칙조회에 실패하였습니다.";
        Boolean success = false;
        List<FindAllRules> findAllRulesList = new ArrayList<>() ;
        try {
            for(RuleDTO ruleDTO:ruleDTOList){
                findAllRulesList.add(new FindAllRules(ruleDTO.getId(), ruleDTO.getRule()));
            }
            success = true;
        }catch (Exception e){
            msg = "규칙조회에 실패하였습니다.";
            success = false;
        }

        return ResponseEntity.ok(new FindAllRuleResponse(success, msg,  findAllRulesList));
    }

    //규칙 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<NullType>> deleteRule(@PathVariable long id){
        Boolean result = ruleService.deleteRules(id);
        String msg = result ? "규칙삭제에 성공하였습니다." : "규칙삭제에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NullType>(result, msg));
    }
    //규칙 업데이트
    @PatchMapping
    public ResponseEntity<ApiResponseDTO<NullType>> updateRule(@RequestBody RuleDTO ruleDTO){
        Boolean result = ruleService.updateRule(ruleDTO);
        String msg = result ? "규칙수정에 성공하였습니다." : "규칙수정에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NullType>(result, msg));
    }

    //규칙리스트 정보 응답
    static class FindAllRuleResponse  extends ApiResponseDTO<List<FindAllRules>>{

        @Builder
        public FindAllRuleResponse(Boolean success, String message, List<FindAllRules> findAllRulesList) {
            super(success, message,findAllRulesList);
        }
    }
    //규칙리스트
    static class FindAllRules{
        @JsonProperty
        private Long id;
        @JsonProperty
        private String rule;

        public FindAllRules(Long id, String rule){
            this.id = id;
            this.rule = rule;
        }
    }
}
