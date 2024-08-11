package com.drinkeg.drinkeg.controller;

import com.drinkeg.drinkeg.apipayLoad.ApiResponse;
import com.drinkeg.drinkeg.domain.Member;
import com.drinkeg.drinkeg.dto.TastingNoteDTO.request.NoteRequestDTO;
import com.drinkeg.drinkeg.dto.TastingNoteDTO.request.NoteUpdateRequestDTO;
import com.drinkeg.drinkeg.dto.TastingNoteDTO.response.NotePriviewResponseDTO;
import com.drinkeg.drinkeg.dto.TastingNoteDTO.response.NoteResponseDTO;
import com.drinkeg.drinkeg.dto.TastingNoteDTO.request.NoteWineRequestDTO;
import com.drinkeg.drinkeg.dto.TastingNoteDTO.response.NoteWineResponseDTO;
import com.drinkeg.drinkeg.dto.securityDTO.jwtDTO.PrincipalDetail;
import com.drinkeg.drinkeg.service.memberService.MemberService;
import com.drinkeg.drinkeg.service.tastingNoteService.TastingNoteService;
import com.drinkeg.drinkeg.service.wineService.WineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasting-note")
public class TastingNoteController {

    private final TastingNoteService tastingNoteService;
    private final WineService wineService;
    private final MemberService memberService;

    // 새 노트 작성
    @PostMapping("/new-note")
    public ApiResponse<String> saveNote(@AuthenticationPrincipal PrincipalDetail principalDetail, @RequestBody @Valid NoteRequestDTO noteRequestDTO) {

        // 현재 로그인한 사용자 정보 가져오기
        String username = principalDetail.getUsername();
        Member foundMember = memberService.findMemberByUsername(username);

        tastingNoteService.saveNote(noteRequestDTO, foundMember);
        return ApiResponse.onSuccess("노트 작성 완료");
    }

    // 새 노트 작성 시 와인 검색
    @GetMapping("/wine")
    public ApiResponse<List<NoteWineResponseDTO>> saveNote(@RequestBody NoteWineRequestDTO noteWineRequestDTO) {

        List<NoteWineResponseDTO> noteWineResponseDTOs = wineService.searchWinesByName(noteWineRequestDTO);
        return ApiResponse.onSuccess(noteWineResponseDTOs);
    }

    // 선택한 노트 보기
    @GetMapping("/{noteId}")
    public ApiResponse<NoteResponseDTO> showNote(@AuthenticationPrincipal PrincipalDetail principalDetail, @PathVariable("noteId") Long noteId) {

        // 현재 로그인한 사용자 정보 가져오기
        String username = principalDetail.getUsername();
        Member foundMember = memberService.findMemberByUsername(username);

        NoteResponseDTO noteResponseDTO = tastingNoteService.showNoteById(noteId);
        return ApiResponse.onSuccess(noteResponseDTO);
    }

    // 전체 노트 보기
    @GetMapping("/all-note")
    public ApiResponse<List<NotePriviewResponseDTO>> showAllNote(@AuthenticationPrincipal PrincipalDetail principalDetail) {

        // 현재 로그인한 사용자 정보 가져오기
        String username = principalDetail.getUsername();
        Member foundMember = memberService.findMemberByUsername(username);

        List<NotePriviewResponseDTO> allNoteByMember = tastingNoteService.findAllNoteByMember(foundMember);
        return ApiResponse.onSuccess(allNoteByMember);
    }

    @PatchMapping("/{noteId}")
    public ApiResponse<String> updateTastingNote(@AuthenticationPrincipal PrincipalDetail principalDetail, @PathVariable("noteId") Long noteId, @RequestBody @Valid NoteUpdateRequestDTO noteUpdateRequestDTO) {

        // 현재 로그인한 사용자 정보 가져오기
        String username = principalDetail.getUsername();
        Member foundMember = memberService.findMemberByUsername(username);

        tastingNoteService.updateTastingNote(noteId, noteUpdateRequestDTO, foundMember);
        return ApiResponse.onSuccess("노트 수정 완료");
    }

    @DeleteMapping("/{noteId}")
    public ApiResponse<String> deleteTastingNote(@AuthenticationPrincipal PrincipalDetail principalDetail, @PathVariable("noteId") Long noteId) {

        // 현재 로그인한 사용자 정보 가져오기
        String username = principalDetail.getUsername();
        Member foundMember = memberService.findMemberByUsername(username);

        tastingNoteService.deleteTastingNote(noteId, foundMember);
        return ApiResponse.onSuccess("노트 삭제 완료");
    }

}
