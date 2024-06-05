package com.growup.ecountry.controller;

import com.growup.ecountry.config.TokenProvider;
import com.growup.ecountry.dto.ApiResponseDTO;
import com.growup.ecountry.dto.NewsDTO;
import com.growup.ecountry.dto.PetitionDTO;
import com.growup.ecountry.dto.TokenDTO;
import com.growup.ecountry.service.PostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.type.NullType;
import java.util.List;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {
    private final PostsService postsService;
    private final TokenProvider jwt;


    //-----------------뉴스------------------------------------------------------------------

    //뉴스 전체 조회
    @GetMapping("/articles/{countryId}")
    public ResponseEntity<ApiResponseDTO<List<NewsDTO>>> findAllNews(@PathVariable("countryId") Long countryId){

        return ResponseEntity.ok(new ApiResponseDTO<List<NewsDTO>>(true, "뉴스 전체 조회에 성공하였습니다.",postsService.findAllNews(countryId )));
    }

    //뉴스 1개 조회
    @GetMapping("/article/{id}")
    public ResponseEntity<ApiResponseDTO<NewsDTO>> findNews(@PathVariable("id") Long id){

        boolean success = false;
        NewsDTO newsDTO = postsService.findNews(id);
        if(newsDTO != null){
            success = true;
        }
        String msg = success ? "뉴스 1개 조회에 성공하였습니다." : "뉴스 1개 조회에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NewsDTO>(success, msg, newsDTO));
    }

    //뉴스 1개 추가
    @PostMapping("/article")
    public ResponseEntity<ApiResponseDTO<NullType>> addNews(@RequestHeader(value = "Authorization") String token, @RequestBody NewsDTO newsDTO){

        TokenDTO authToken = jwt.validateToken(token);
        if(authToken.getIsStudent() ) {
            newsDTO.setWriterId(authToken.getId());
        } else {
            newsDTO.setWriterId(null);
        }
        boolean success = postsService.addNews(newsDTO);
        String msg = success ? "뉴스 1개 추가에 성공하였습니다." : "뉴스 1개 추가에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success, msg));

    }
    //뉴스 1개 삭제
    @DeleteMapping("/article/{id}")
    public ResponseEntity<ApiResponseDTO<NullType>> deleteNews(@PathVariable("id") Long id){
        boolean success = postsService.deleteNews(id);
        String msg = success ? "뉴스 1개 삭제에 성공하였습니다." : "뉴스 1개 삭제에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success, msg));
    }

    //뉴스 1개 수정
    @PatchMapping("/article")
    public ResponseEntity<ApiResponseDTO<NullType>> updateNews(@RequestBody NewsDTO newsDTO){
        boolean success = postsService.updateNews(newsDTO);
        String msg = success ? "뉴스 1개 수정에 성공하였습니다." : "뉴스 1개 수정에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success, msg));
    }
    //-----------------신문고------------------------------------------------------------------

    //신문고 전체 조회
    @GetMapping("/petitions/{countryId}")
    public ResponseEntity<ApiResponseDTO<List<PetitionDTO>>> findAllPetitions(@PathVariable("countryId") Long countryId){

        return ResponseEntity.ok(new ApiResponseDTO<List<PetitionDTO>>(true, "신문고 전체 조회에 성공하였습니다.",postsService.findAllPetitions(countryId )));
    }

    //신문고 1개 조회
    @GetMapping("/petition/{id}")
    public ResponseEntity<ApiResponseDTO<PetitionDTO>> findPetition(@PathVariable("id") Long id){

        boolean success = false;
        PetitionDTO petitionDTO = postsService.findPetition(id);
        if(petitionDTO != null){
            success = true;
        }
        String msg = success ? "신문고 1개 조회에 성공하였습니다." : "신문고 1개 조회에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<PetitionDTO>(success, msg, petitionDTO));
    }

    //신문고 1개 추가
    @PostMapping("/petition")
    public ResponseEntity<ApiResponseDTO<NullType>> addPetition(@RequestHeader(value = "Authorization") String token, @RequestBody PetitionDTO petitionDTO){

        TokenDTO authToken = jwt.validateToken(token);
        if(authToken.getIsStudent() ) {
            System.out.println("id : "+ authToken.getId());
            petitionDTO.setWriterId(authToken.getId());
        } else {
            petitionDTO.setWriterId(null);
        }
        boolean success = postsService.addPetition(petitionDTO);
        String msg = success ? "신문고 1개 추가에 성공하였습니다." : "신문고 1개 추가에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success, msg));

    }
    //신문고 1개 삭제
    @DeleteMapping("/petition/{id}")
    public ResponseEntity<ApiResponseDTO<NullType>> deletePetition(@PathVariable("id") Long id){
        boolean success = postsService.deletePetition(id);
        String msg = success ? "신문고 1개 삭제에 성공하였습니다." : "신문고 1개 삭제에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success, msg));
    }

    //신문고 1개 수정
    @PatchMapping("/petition")
    public ResponseEntity<ApiResponseDTO<NullType>> updatePetition(@RequestBody PetitionDTO petitionDTO){
        boolean success = postsService.updatePetition(petitionDTO);
        String msg = success ? "신문고 1개 수정에 성공하였습니다." : "신문고 1개 수정에 실패하였습니다.";

        return ResponseEntity.ok(new ApiResponseDTO<NullType>(success, msg));
    }


}
