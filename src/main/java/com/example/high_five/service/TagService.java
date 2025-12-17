package com.example.high_five.service;

import com.example.high_five.dto.Tag.request.TagRequest;
import com.example.high_five.dto.book.response.BookResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "tag-service", contextId = "tagClient", url = "${gateway.uri}")
public interface TagService {

    @PostMapping("/api/tag")
    BookResponse createTag(@RequestBody TagRequest tagRequest);

    // 다량 조회
    @GetMapping("/api/tag")
    List<BookResponse> getTags();

    // 한건 조
    @GetMapping("/api/tag/{tagId}")
    BookResponse getTag(@PathVariable("tagId") Long tagId);
}
