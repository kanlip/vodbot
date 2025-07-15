package com.example.demo.video;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/video")
public class VideoController {
    // Dummy video DTO for demonstration
    public static class VideoDto {
        public String id;
        public String title;
        public String url;

        public VideoDto(String id, String title, String url) {
            this.id = id;
            this.title = title;
            this.url = url;
        }
    }

    @GetMapping
    public Page<VideoDto> getVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort
    ) {
        // Example: sort=["title,desc"]
        Sort sortObj = Sort.by(
                Stream.of(sort).map(s -> {
                    String[] parts = s.split(",");
                    return new Sort.Order(
                            parts.length > 1 && parts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                            parts[0]
                    );
                }).toList()
        );
        Pageable pageable = PageRequest.of(page, size, sortObj);
        // Dummy data for demonstration
        List<VideoDto> allVideos = List.of(
                new VideoDto("1", "Video A", "url1"),
                new VideoDto("2", "Video B", "url2"),
                new VideoDto("3", "Video C", "url3")
        );
        // In real code, fetch from DB with pageable
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allVideos.size());
        List<VideoDto> paged = allVideos.subList(Math.min(start, allVideos.size()), end);
        return new PageImpl<>(paged, pageable, allVideos.size());
    }
}
