package com.thienan.lovebox.service;

import com.thienan.lovebox.utils.PagedResponse;
import com.thienan.lovebox.shared.dto.SingleQuestionDto;

public interface SingleQuestionService {

    PagedResponse<SingleQuestionDto> getQuestionsByUserId(Long userId, boolean answered, int page, int size);

    SingleQuestionDto getQuestion(Long id);

    SingleQuestionDto createQuestion(SingleQuestionDto singleQuestionDto);

    SingleQuestionDto answeredQuestion(Long id, String answerText);
}
