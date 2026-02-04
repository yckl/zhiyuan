package com.volunteer.vo;

import com.volunteer.dto.VolunteerDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 志愿者综合统计VO
 */
@Data
@Schema(description = "志愿者综合统计")
public class VolunteerStatsVO {

    @Schema(description = "基础个人信息")
    private VolunteerDTO profile;

    @Schema(description = "雷达图数据(分类-次数)")
    private Map<String, Integer> radarData;

    @Schema(description = "热力图数据(日期-次数)")
    private List<HeatmapItem> heatmapData;

    @Schema(description = "荣誉证书列表")
    private List<HonorItem> honors;

    @Data
    public static class HeatmapItem {
        private String date;
        private Integer value;
    }

    @Data
    public static class HonorItem {
        private Long id;
        private String name;
        private String date;
        private String image; // Base64 or URL
    }
}
