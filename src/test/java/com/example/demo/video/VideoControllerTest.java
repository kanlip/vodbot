package com.example.demo.video;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.demo.video.entity.VideoEntity;
import com.example.demo.video.repository.VideoRepository;
import com.example.demo.video.repository.VideoRepositoryCustom;
import java.time.Instant;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class VideoControllerTest {

    @Mock
    private VideoRepositoryCustom videoRepository;

    @InjectMocks
    private VideoController videoController;

    private ObjectId companyId;
    private ObjectId orderId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        companyId = new ObjectId();
        orderId = new ObjectId();
    }

    @Test
    void testSearchVideosWithCompanyIdOnly() {
        System.out.println(
            "[DEBUG_LOG] Testing search videos with company ID only"
        );

        // Create mock video
        VideoEntity mockVideo = createMockVideo();
        Page<VideoEntity> mockPage = new PageImpl<>(List.of(mockVideo));

        // Mock repository call
        when(
            videoRepository.searchVideosWithMultipleCriteria(
                eq(companyId),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
            )
        ).thenReturn(mockPage);

        // Call controller method
        Page<VideoController.VideoDto> result = videoController.searchVideos(
            companyId.toString(),
            null,
            null,
            null,
            null,
            null,
            0,
            10,
            "recordedAt,desc"
        );

        System.out.println(
            "[DEBUG_LOG] Search returned " +
            result.getTotalElements() +
            " videos"
        );

        // Verify results
        assertEquals(1, result.getTotalElements());
        assertEquals(mockVideo.getId(), result.getContent().get(0).id);
        assertEquals(
            mockVideo.getRecordedByUserId().toHexString(),
            result.getContent().get(0).title
        );
        assertEquals(mockVideo.getVideoUrl(), result.getContent().get(0).url);

        // Verify repository was called with correct parameters
        verify(videoRepository).searchVideosWithMultipleCriteria(
            eq(companyId),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            any(Pageable.class)
        );
    }

    @Test
    void testSearchVideosWithSku() {
        System.out.println("[DEBUG_LOG] Testing search videos with SKU");

        VideoEntity mockVideo = createMockVideo();
        Page<VideoEntity> mockPage = new PageImpl<>(List.of(mockVideo));

        when(
            videoRepository.searchVideosWithMultipleCriteria(
                eq(companyId),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq("SKU001"),
                any(Pageable.class)
            )
        ).thenReturn(mockPage);

        Page<VideoController.VideoDto> result = videoController.searchVideos(
            companyId.toString(),
            null,
            null,
            null,
            null,
            "SKU001",
            0,
            10,
            "recordedAt,desc"
        );

        System.out.println(
            "[DEBUG_LOG] Search with SKU returned " +
            result.getTotalElements() +
            " videos"
        );

        assertEquals(1, result.getTotalElements());
        verify(videoRepository).searchVideosWithMultipleCriteria(
            eq(companyId),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            eq("SKU001"),
            any(Pageable.class)
        );
    }

    @Test
    void testSearchVideosWithOrderId() {
        System.out.println("[DEBUG_LOG] Testing search videos with order ID");

        VideoEntity mockVideo = createMockVideo();
        Page<VideoEntity> mockPage = new PageImpl<>(List.of(mockVideo));

        when(
            videoRepository.searchVideosWithMultipleCriteria(
                eq(companyId),
                isNull(),
                isNull(),
                eq(orderId),
                isNull(),
                isNull(),
                any(Pageable.class)
            )
        ).thenReturn(mockPage);

        Page<VideoController.VideoDto> result = videoController.searchVideos(
            companyId.toString(),
            null,
            null,
            orderId.toString(),
            null,
            null,
            0,
            10,
            "recordedAt,desc"
        );

        System.out.println(
            "[DEBUG_LOG] Search with order ID returned " +
            result.getTotalElements() +
            " videos"
        );

        assertEquals(1, result.getTotalElements());
        verify(videoRepository).searchVideosWithMultipleCriteria(
            eq(companyId),
            isNull(),
            isNull(),
            eq(orderId),
            isNull(),
            isNull(),
            any(Pageable.class)
        );
    }

    @Test
    void testSearchVideosWithPlatformOrderId() {
        System.out.println(
            "[DEBUG_LOG] Testing search videos with platform order ID"
        );

        VideoEntity mockVideo = createMockVideo();
        Page<VideoEntity> mockPage = new PageImpl<>(List.of(mockVideo));

        when(
            videoRepository.searchVideosWithMultipleCriteria(
                eq(companyId),
                isNull(),
                isNull(),
                isNull(),
                eq("PLATFORM-001"),
                isNull(),
                any(Pageable.class)
            )
        ).thenReturn(mockPage);

        Page<VideoController.VideoDto> result = videoController.searchVideos(
            companyId.toString(),
            null,
            null,
            null,
            "PLATFORM-001",
            null,
            0,
            10,
            "recordedAt,desc"
        );

        System.out.println(
            "[DEBUG_LOG] Search with platform order ID returned " +
            result.getTotalElements() +
            " videos"
        );

        assertEquals(1, result.getTotalElements());
        verify(videoRepository).searchVideosWithMultipleCriteria(
            eq(companyId),
            isNull(),
            isNull(),
            isNull(),
            eq("PLATFORM-001"),
            isNull(),
            any(Pageable.class)
        );
    }

    @Test
    void testSearchVideosWithDateRange() {
        System.out.println("[DEBUG_LOG] Testing search videos with date range");

        VideoEntity mockVideo = createMockVideo();
        Page<VideoEntity> mockPage = new PageImpl<>(List.of(mockVideo));

        when(
            videoRepository.searchVideosWithMultipleCriteria(
                eq(companyId),
                any(Instant.class),
                any(Instant.class),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
            )
        ).thenReturn(mockPage);

        Page<VideoController.VideoDto> result = videoController.searchVideos(
            companyId.toString(),
            "2024-01-01",
            "2024-01-02",
            null,
            null,
            null,
            0,
            10,
            "recordedAt,desc"
        );

        System.out.println(
            "[DEBUG_LOG] Search with date range returned " +
            result.getTotalElements() +
            " videos"
        );

        assertEquals(1, result.getTotalElements());
        verify(videoRepository).searchVideosWithMultipleCriteria(
            eq(companyId),
            any(Instant.class),
            any(Instant.class),
            isNull(),
            isNull(),
            isNull(),
            any(Pageable.class)
        );
    }

    @Test
    void testSearchVideosWithInvalidCompanyId() {
        System.out.println(
            "[DEBUG_LOG] Testing search videos with invalid company ID"
        );

        assertThrows(IllegalArgumentException.class, () -> {
            videoController.searchVideos(
                "invalid-company-id",
                null,
                null,
                null,
                null,
                null,
                0,
                10,
                "recordedAt,desc"
            );
        });

        System.out.println(
            "[DEBUG_LOG] Invalid company ID correctly threw IllegalArgumentException"
        );
    }

    @Test
    void testSearchVideosWithInvalidOrderId() {
        System.out.println(
            "[DEBUG_LOG] Testing search videos with invalid order ID"
        );

        assertThrows(IllegalArgumentException.class, () -> {
            videoController.searchVideos(
                companyId.toString(),
                null,
                null,
                "invalid-order-id",
                null,
                null,
                0,
                10,
                "recordedAt,desc"
            );
        });

        System.out.println(
            "[DEBUG_LOG] Invalid order ID correctly threw IllegalArgumentException"
        );
    }

    private VideoEntity createMockVideo() {
        VideoEntity video = new VideoEntity();
        video.setId("test-video-id");
        video.setCompanyId(companyId);
        video.setOrderId(orderId);
        video.setPlatformOrderId("PLATFORM-001");
        video.setRecordedByUserId(new ObjectId());
        video.setVideoUrl("https://example.com/video.mp4");
        video.setRecordedAt(Instant.parse("2024-01-01T10:00:00Z"));
        video.setStatus("available");

        VideoEntity.ItemScan itemScan = new VideoEntity.ItemScan();
        itemScan.setSku("SKU001");
        itemScan.setQuantity(1);
        itemScan.setStatus("scanned");
        video.setItemScans(List.of(itemScan));

        return video;
    }
}
