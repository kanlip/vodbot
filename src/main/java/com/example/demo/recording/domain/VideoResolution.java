package com.example.demo.recording.domain;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class VideoResolution {
    private final int width;
    private final int height;

    public static final VideoResolution HD_720P = new VideoResolution(1280, 720);
    public static final VideoResolution FULL_HD_1080P = new VideoResolution(1920, 1080);
    public static final VideoResolution UHD_4K = new VideoResolution(3840, 2160);

    public static VideoResolution fromString(String resolution) {
        if (resolution == null || resolution.trim().isEmpty()) {
            return null;
        }

        String[] parts = resolution.split("x");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid resolution format. Expected format: WIDTHxHEIGHT");
        }

        try {
            int width = Integer.parseInt(parts[0].trim());
            int height = Integer.parseInt(parts[1].trim());
            return new VideoResolution(width, height);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid resolution format. Width and height must be numbers");
        }
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }

    public boolean isHighDefinition() {
        return width >= 1280 && height >= 720;
    }

    public boolean isFullHD() {
        return width >= 1920 && height >= 1080;
    }

    public double getAspectRatio() {
        return (double) width / height;
    }

    public long getPixelCount() {
        return (long) width * height;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VideoResolution that = (VideoResolution) obj;
        return width == that.width && height == that.height;
    }

    @Override
    public int hashCode() {
        return 31 * width + height;
    }
}