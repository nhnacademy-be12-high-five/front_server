package com.example.high_five.service;

import com.example.high_five.dto.book.TagUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "gateway-server", url = "${gateway.uri}")
public interface BookUserTagClient {

    @GetMapping("/api/books/{bookId}/user-tags")
    List<String> getUserTags(@RequestHeader("X-MEMBER-ID") Long memberId,
                             @PathVariable Long bookId);

    @PostMapping("/api/books/{bookId}/user-tags")
    void addUserTag(@RequestHeader("X-MEMBER-ID") Long memberId,
                    @PathVariable Long bookId,
                    @RequestBody TagUpdateRequest request);

    @DeleteMapping("/api/books/{bookId}/user-tags/{tagCode}")
    void removeUserTag(@RequestHeader("X-MEMBER-ID") Long memberId,
                       @PathVariable Long bookId,
                       @PathVariable String tagCode);
}
