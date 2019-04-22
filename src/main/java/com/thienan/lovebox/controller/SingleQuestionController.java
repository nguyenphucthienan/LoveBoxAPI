package com.thienan.lovebox.controller;

import com.thienan.lovebox.exception.ForbiddenException;
import com.thienan.lovebox.payload.request.AskSingleQuestionRequest;
import com.thienan.lovebox.payload.response.SingleQuestionReponse;
import com.thienan.lovebox.security.CurrentUser;
import com.thienan.lovebox.security.UserPrincipal;
import com.thienan.lovebox.service.SingleQuestionService;
import com.thienan.lovebox.service.UserService;
import com.thienan.lovebox.shared.dto.SingleQuestionDto;
import com.thienan.lovebox.shared.dto.UserDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/single-questions")
public class SingleQuestionController {

    @Autowired
    UserService userService;

    @Autowired
    SingleQuestionService singleQuestionService;

    @GetMapping()
    @PreAuthorize("hasRole('USER')")
    public List<SingleQuestionReponse> getQuestions(@CurrentUser UserPrincipal currentUser,
                                                    @PathVariable("userId") Long userId,
                                                    @RequestParam(value = "answered", defaultValue = "false") boolean answered,
                                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                                    @RequestParam(value = "limit", defaultValue = "20") int limit) {
        if (userId != currentUser.getId()) {
            throw new ForbiddenException("Cannot get questions of this user");
        }

        if (page > 0) {
            page--;
        }

        List<SingleQuestionDto> questions = singleQuestionService.getQuestionsByUserId(userId, answered, page, limit);
        List<SingleQuestionReponse> returnQuestions = new ArrayList<>();

        ModelMapper modelMapper = new ModelMapper();

        for (SingleQuestionDto singleQuestionDto : questions) {
            SingleQuestionReponse singleQuestionReponse = modelMapper.map(singleQuestionDto, SingleQuestionReponse.class);
            returnQuestions.add(singleQuestionReponse);
        }

        return returnQuestions;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public SingleQuestionReponse askSingleQuestion(@CurrentUser UserPrincipal currentUser,
                                                   @PathVariable("userId") Long answererId,
                                                   @RequestBody AskSingleQuestionRequest askSingleQuestionRequest) {
        UserDto questioner = userService.getUserById(currentUser.getId());
        UserDto answerer = userService.getUserById(answererId);

        ModelMapper modelMapper = new ModelMapper();

        SingleQuestionDto singleQuestionDto = modelMapper.map(askSingleQuestionRequest, SingleQuestionDto.class);
        singleQuestionDto.setQuestioner(questioner);
        singleQuestionDto.setAnswerer(answerer);

        SingleQuestionDto createdQuestion = singleQuestionService.createQuestion(singleQuestionDto);
        SingleQuestionReponse singleQuestionReponse = modelMapper.map(createdQuestion, SingleQuestionReponse.class);

        return singleQuestionReponse;
    }
}
