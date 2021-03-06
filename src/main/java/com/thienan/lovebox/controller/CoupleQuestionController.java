package com.thienan.lovebox.controller;

import com.thienan.lovebox.exception.BadRequestException;
import com.thienan.lovebox.exception.ForbiddenException;
import com.thienan.lovebox.payload.request.AnswerCoupleQuestionRequest;
import com.thienan.lovebox.payload.request.AskCoupleQuestionRequest;
import com.thienan.lovebox.payload.response.ApiResponse;
import com.thienan.lovebox.payload.response.CoupleQuestionResponse;
import com.thienan.lovebox.security.CurrentUser;
import com.thienan.lovebox.security.UserPrincipal;
import com.thienan.lovebox.service.CoupleQuestionService;
import com.thienan.lovebox.service.UserService;
import com.thienan.lovebox.shared.dto.BffDetailDto;
import com.thienan.lovebox.shared.dto.CoupleQuestionDto;
import com.thienan.lovebox.shared.dto.UserDto;
import com.thienan.lovebox.utils.PagedResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/couple-questions")
public class CoupleQuestionController {

    @Autowired
    UserService userService;

    @Autowired
    CoupleQuestionService coupleQuestionService;

    @Autowired
    ModelMapper modelMapper;

    @GetMapping("/news-feed")
    @PreAuthorize("hasRole('USER')")
    public PagedResponse<CoupleQuestionResponse> getQuestionsInNewsFeed(@CurrentUser UserPrincipal currentUser,
                                                                        @PathVariable("userId") Long userId,
                                                                        Pageable pageable) {
        if (!userId.equals(currentUser.getId())) {
            throw new ForbiddenException("Cannot get news feed of this user");
        }

        PagedResponse<CoupleQuestionDto> questions = coupleQuestionService.getQuestionsInNewsFeed(userId, pageable);
        return mapToCoupleQuestionResponsePage(questions);
    }

    @GetMapping()
    @PreAuthorize("hasRole('USER')")
    public PagedResponse<CoupleQuestionResponse> getQuestions(@CurrentUser UserPrincipal currentUser,
                                                              @PathVariable("userId") Long userId,
                                                              @RequestParam(value = "answered", defaultValue = "false") boolean answered,
                                                              Pageable pageable) {
        if (!userId.equals(currentUser.getId()) && !answered) {
            throw new ForbiddenException("Cannot get questions of this user");
        }

        PagedResponse<CoupleQuestionDto> questions = coupleQuestionService.getQuestionsByUserId(userId, answered, pageable);
        return mapToCoupleQuestionResponsePage(questions);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public CoupleQuestionResponse askCoupleQuestion(@CurrentUser UserPrincipal currentUser,
                                                    @PathVariable("userId") Long userId,
                                                    @Valid @RequestBody AskCoupleQuestionRequest askCoupleQuestionRequest) {
        UserDto questioner = userService.getUserById(currentUser.getId());
        UserDto userDto = userService.getUserById(userId);
        BffDetailDto bffDetailDto = userDto.getBffDetail();

        if (bffDetailDto == null) {
            throw new BadRequestException("This user does not have BFF");
        }

        if (bffDetailDto.getFirstUser().getId().equals(currentUser.getId())
                || bffDetailDto.getSecondUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Cannot ask question");
        }

        CoupleQuestionDto coupleQuestionDto = modelMapper.map(askCoupleQuestionRequest, CoupleQuestionDto.class);
        coupleQuestionDto.setQuestioner(questioner);
        coupleQuestionDto.setFirstAnswerer(bffDetailDto.getFirstUser());
        coupleQuestionDto.setSecondAnswerer(bffDetailDto.getSecondUser());

        CoupleQuestionDto createdQuestion = coupleQuestionService.createQuestion(coupleQuestionDto);
        return mapToCoupleQuestionResponse(createdQuestion);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public CoupleQuestionResponse getCoupleQuestion(@CurrentUser UserPrincipal currentUser,
                                                    @PathVariable("userId") Long userId,
                                                    @PathVariable("id") Long id) {
        CoupleQuestionDto coupleQuestionDto = coupleQuestionService.getQuestion(id);

        if (!coupleQuestionDto.getFirstAnswerer().getId().equals(userId)
                && !coupleQuestionDto.getSecondAnswerer().getId().equals(userId)) {
            throw new BadRequestException("User ID and Question ID do not match");
        }

        if ((!coupleQuestionDto.getFirstAnswerer().getId().equals(currentUser.getId())
                && !coupleQuestionDto.getSecondAnswerer().getId().equals(currentUser.getId()))
                && !coupleQuestionDto.isAnswered()) {
            throw new BadRequestException("Question has not been answered");
        }

        return mapToCoupleQuestionResponse(coupleQuestionDto);
    }

    @PostMapping("/{id}/answer")
    @PreAuthorize("hasRole('USER')")
    public CoupleQuestionResponse answerCoupleQuestion(@CurrentUser UserPrincipal currentUser,
                                                       @PathVariable("userId") Long userId,
                                                       @PathVariable("id") Long id,
                                                       @Valid @RequestBody AnswerCoupleQuestionRequest answerCoupleQuestionRequest) {
        CoupleQuestionDto coupleQuestionDto = coupleQuestionService.getQuestion(id);

        if (!coupleQuestionDto.getFirstAnswerer().getId().equals(userId)
                && !coupleQuestionDto.getSecondAnswerer().getId().equals(userId)) {
            throw new BadRequestException("User ID and Question ID do not match");
        }

        if (!coupleQuestionDto.getFirstAnswerer().getId().equals(currentUser.getId())
                && !coupleQuestionDto.getSecondAnswerer().getId().equals(userId)) {
            throw new ForbiddenException("Cannot answer this question");
        }

        CoupleQuestionDto answeredCoupleQuestionDto = coupleQuestionService
                .answerQuestion(id, currentUser.getId(), answerCoupleQuestionRequest.getAnswerText());

        return mapToCoupleQuestionResponse(answeredCoupleQuestionDto);
    }

    @PostMapping("/{id}/unanswer")
    @PreAuthorize("hasRole('USER')")
    public CoupleQuestionResponse unanswerCoupleQuestion(@CurrentUser UserPrincipal currentUser,
                                                         @PathVariable("userId") Long userId,
                                                         @PathVariable("id") Long id) {
        CoupleQuestionDto coupleQuestionDto = coupleQuestionService.getQuestion(id);

        if (!coupleQuestionDto.getFirstAnswerer().getId().equals(userId)
                && !coupleQuestionDto.getSecondAnswerer().getId().equals(userId)) {
            throw new BadRequestException("User ID and Question ID do not match");
        }

        if (!coupleQuestionDto.getFirstAnswerer().getId().equals(currentUser.getId())
                && !coupleQuestionDto.getSecondAnswerer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Cannot unanswer this question");
        }

        CoupleQuestionDto unansweredCoupleQuestionDto = coupleQuestionService.unanswerQuestion(id);
        return mapToCoupleQuestionResponse(unansweredCoupleQuestionDto);
    }

    @PostMapping("/{id}/love")
    @PreAuthorize("hasRole('USER')")
    public CoupleQuestionResponse loveOrUnloveCoupleQuestion(@CurrentUser UserPrincipal currentUser,
                                                             @PathVariable("userId") Long userId,
                                                             @PathVariable("id") Long id) {
        CoupleQuestionDto coupleQuestionDto = coupleQuestionService.getQuestion(id);

        if (!coupleQuestionDto.getFirstAnswerer().getId().equals(userId)
                && !coupleQuestionDto.getSecondAnswerer().getId().equals(userId)) {
            throw new BadRequestException("User ID and Question ID do not match");
        }

        if (!coupleQuestionDto.isAnswered()) {
            throw new BadRequestException("Question has not been answered");
        }

        CoupleQuestionDto lovedCoupleQuestionDto = coupleQuestionService.loveOrUnloveQuestion(id, currentUser.getId());
        return mapToCoupleQuestionResponse(lovedCoupleQuestionDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse deleteCoupleQuestion(@CurrentUser UserPrincipal currentUser,
                                            @PathVariable("userId") Long userId,
                                            @PathVariable("id") Long id) {
        CoupleQuestionDto coupleQuestionDto = coupleQuestionService.getQuestion(id);

        if (!coupleQuestionDto.getFirstAnswerer().getId().equals(userId)
                && !coupleQuestionDto.getSecondAnswerer().getId().equals(userId)) {
            throw new BadRequestException("User ID and Question ID do not match");
        }

        if (!coupleQuestionDto.getFirstAnswerer().getId().equals(currentUser.getId())
                && !coupleQuestionDto.getSecondAnswerer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Cannot delete this question");
        }

        coupleQuestionService.deleteQuestion(id);
        return new ApiResponse(true, "Delete couple question successfully");
    }

    private CoupleQuestionResponse mapToCoupleQuestionResponse(CoupleQuestionDto coupleQuestionDto) {
        return modelMapper.map(coupleQuestionDto, CoupleQuestionResponse.class);
    }

    private PagedResponse<CoupleQuestionResponse> mapToCoupleQuestionResponsePage(PagedResponse<CoupleQuestionDto> coupleQuestionDtos) {
        List<CoupleQuestionResponse> coupleQuestionResponses = modelMapper.map(
                coupleQuestionDtos.getContent(),
                new TypeToken<List<CoupleQuestionResponse>>() {
                }.getType()
        );

        return new PagedResponse<>(coupleQuestionResponses, coupleQuestionDtos.getPagination());
    }
}
