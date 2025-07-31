package com.example.demo.video;

import com.example.demo.video.entity.VideoEntity;
import com.example.demo.video.repository.VideoRepository;
import com.example.demo.video.repository.VideoRepositoryCustom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
// [when order being pushed to system webhook send hook to orders and to videos with group truth with MISSING FLAG
// SO WHEN USER SCAN WE UPDATE THE FLAG AND VIDEO URL]
public class VideoController {

    private final VideoRepositoryCustom videoRepository;
    private final VideoRepository videoMongoRepository;

    @Getter
    public class VideoDto {

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
            this.title = entity.getRecordedByUserId().toHexString(); // Example: use packerName as title
            this.url = entity.getVideoUrl();
        }
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Void> updateVideo(
        @PathVariable String orderId,
        @RequestBody VideoDto videoDto
    ) {
        // VideoEntity entity = videoMongoRepository
        //     .findById(orderId)
        //     .orElseThrow(() -> new RuntimeException("Video not found"));
        // entity.setVideoUrl(videoDto.getVideoUrl());
        // videoRepository.save(entity);
        return ResponseEntity.ok().build();
    }

    /**
     * Search videos with multiple criteria.
     *
     * @param companyId         The ID of the company.
     * @param startDate         The start date for filtering videos (optional).
     * @param endDate           The end date for filtering videos (optional).
     * @param orderId           The order ID to filter videos (optional).
     * @param platformOrderId   The platform order ID to filter videos (optional).
     * @param sku               The SKU to filter videos (optional).
     * @param page              The page number for pagination.
     * @param size              The size of each page.
     * @param sort              The sorting criteria, e.g., "recordedAt,desc".
     * @return A paginated list of video DTOs.
     */
    @GetMapping
    public Page<VideoDto> searchVideos(
        @RequestParam String companyId,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @RequestParam(required = false) String orderId,
        @RequestParam(required = false) String platformOrderId,
        @RequestParam(required = false) String sku,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(
            defaultValue = "recordedAt,desc",
            name = "sort"
        ) String sort
    ) {
        // Parse companyId
        ObjectId companyObjectId;
        try {
            companyObjectId = new ObjectId(companyId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid companyId format");
        }

        // Parse sort parameters
        String[] sortParams = sort.contains("&")
            ? sort.split("&")
            : sort.split(",");
        List<Sort.Order> orders = new java.util.ArrayList<>();
        for (int i = 0; i < sortParams.length; i++) {
            String[] parts = sortParams[i].split(",");
            if (parts.length == 2) {
                orders.add(
                    new Sort.Order(
                        parts[1].equalsIgnoreCase("desc")
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC,
                        parts[0]
                    )
                );
            } else if (parts.length == 1 && i + 1 < sortParams.length) {
                orders.add(
                    new Sort.Order(
                        sortParams[i + 1].equalsIgnoreCase("desc")
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC,
                        parts[0]
                    )
                );
                i++;
            }
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));

        // Parse date parameters
        Instant startInstant = null;
        Instant endInstant = null;
        if (startDate != null && !startDate.isEmpty()) {
            startInstant = LocalDate.parse(startDate)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);
        }
        if (endDate != null && !endDate.isEmpty()) {
            endInstant = LocalDate.parse(endDate)
                .plusDays(1)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);
        }

        // Parse orderId
        ObjectId orderObjectId = null;
        if (orderId != null && !orderId.isEmpty()) {
            try {
                orderObjectId = new ObjectId(orderId);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid orderId format");
            }
        }

        // Use the custom search method that can combine multiple criteria
        Page<VideoEntity> videoPage =
            videoRepository.searchVideosWithMultipleCriteria(
                companyObjectId,
                startInstant,
                endInstant,
                orderObjectId,
                platformOrderId,
                sku,
                pageable
            );

        return videoPage.map(VideoDto::new);
    }
}
