package com.example.demo.video;

import com.example.demo.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VideoMockDataLoader implements CommandLineRunner {

    private final VideoRepository videoRepository;

    @Override
    public void run(String... args) {
        // Mock CompanyEntity and BarcodeEntity references (replace with real fetch in real code)
        //        ItemScan scan1 = new ItemScan();
        //        scan1.setTimestampOffsetSeconds(15);
        //        scan1.setSku("ITEM001");
        //        scan1.setQuantity(1);
        //        scan1.setStatus("scanned");
        //        ItemScan scan2 = new ItemScan();
        //        scan2.setTimestampOffsetSeconds(60);
        //        scan2.setSku("ITEM002");
        //        scan2.setQuantity(2);
        //        scan2.setStatus("scanned");
        //
        //        VideoEntity video = new VideoEntity();
        //        video.setOrderId(new ObjectId("665f80b1a2b3c4d5e6f7a8bc"));
        //        video.setPlatformOrderId("LAZADA-ORD-987654321");
        //        video.setRecordedByUserId(new ObjectId("665f80b1a2b3c4d5e6f7a8ba"));
        //        video.setPackerName("Kan Pakker");
        //        video.setS3Key(
        //            "videos/665f80b1a2b3c4d5e6f7a8b9/LAZADA-ORD-987654321_20250605143500.mp4"
        //        );
        //        video.setS3Bucket("your-saas-packing-videos");
        //        video.setVideoUrl(
        //            "https://your-saas-packing-videos.s3.ap-southeast-1.amazonaws.com/videos/..."
        //        );
        //        video.setCompanyId(new ObjectId());
        //        video.setRecordedAt(Instant.parse("2025-06-05T14:35:00Z"));
        //        video.setDurationSeconds(300);
        //        video.setFileSizeMB(50.5);
        //        video.setResolution("1080p");
        //        video.setStatus("available");
        //        video.setNotes("All items checked and securely packed.");
        //        video.setItemScans(List.of(scan1, scan2));
        //        video.setDisputeStatus("none");
        //        video.setCreatedAt(Instant.parse("2025-06-05T14:35:05Z"));
        //        video.setUpdatedAt(Instant.parse("2025-06-05T14:35:05Z"));
        //
        //        videoRepository.save(video);
    }
}
