package com.example.demo.video;

import com.example.demo.video.entity.VideoEntity;
import com.example.demo.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequiredArgsConstructor
public class VideoController {
    private final VideoRepository videoRepository;

    public static class VideoDto {
        public String id;
        public String title;
        public String url;

        public VideoDto(String id, String title, String url) {
            this.id = id;
            this.title = title;
            this.url = url;
        }

        public VideoDto(VideoEntity entity) {
            this.id = entity.getId();
            this.title = entity.getPackerName(); // Example: use packerName as title
            this.url = entity.getVideoUrl();
        }
    }

    @GetMapping
    public Page<VideoDto> getVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc", name = "sort") String sort
    ) {
        // Accepts: sort=title,asc&sort=id,desc or sort=title,asc,id,desc
        String[] sortParams = sort.contains("&") ? sort.split("&") : sort.split(",");
        List<Sort.Order> orders = new java.util.ArrayList<>();
        for (int i = 0; i < sortParams.length; i++) {
            String[] parts = sortParams[i].split(",");
            if (parts.length == 2) {
                orders.add(new Sort.Order(
                        parts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                        parts[0]
                ));
            } else if (parts.length == 1 && i + 1 < sortParams.length) {
                // Support: sort=title,asc,id,desc
                orders.add(new Sort.Order(
                        sortParams[i + 1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                        parts[0]
                ));
                i++;
            }
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        Page<VideoEntity> videoPage = videoRepository.findAll(pageable);
        return videoPage.map(VideoDto::new);
    }
}
