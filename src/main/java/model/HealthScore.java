package model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import constant.Constant;
import enums.Metric;
import enums.StatisticData;
import enums.Strategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthScore implements Comparable<HealthScore> {

  private Long repoId;

  private String repoName;

  @Builder.Default
  private ConcurrentMap<Metric, Double> singleMetricScores = new ConcurrentHashMap<>();

  @Builder.Default
  private ConcurrentMap<StatisticData, Number> statisticData = new ConcurrentHashMap<>();

  private Double score;

  public String[] toCsvRow() {
    List<String> singleMetricScoresStr = Stream.of(Metric.values()).map(singleMetricScores::get)
        .map(String::valueOf).collect(Collectors.toList());

    List<String> statisticDataStr = Stream.of(StatisticData.values()).map(statisticData::get)
        .map(String::valueOf).collect(Collectors.toList());
    
    List<String> row = new ArrayList<>();

    row.add(String.valueOf(repoId));
    row.add(repoName);
    row.add(String.valueOf(score));
    
   
    // add single scores
    row.addAll(singleMetricScoresStr);
    
    // add statistic data
    row.addAll(statisticDataStr);
    
    return row.toArray(new String[0]);
  };

  public static HealthScore.HealthScoreBuilder commonBuilder(Strategy metricGroup) {
    return HealthScore.builder().singleMetricScores(initSingleMetricMap(metricGroup));

  }

  private static ConcurrentMap<Metric, Double> initSingleMetricMap(Strategy metricGroup) {
    ConcurrentMap<Metric, Double> singleMetricScores = new ConcurrentHashMap<>();

    // init by metrics
    metricGroup.getMetrics()
        .forEach(metric -> singleMetricScores.put(metric, Constant.DEFAULT_SCORE));

    return singleMetricScores;
  }

  @Override
  public int compareTo(HealthScore other) {
    return Double.compare(this.score, other.getScore());
  }
}
