package com.blackmorse.hattrick;

import com.blackmorse.hattrick.api.Hattrick;
import com.blackmorse.hattrick.api.LeagueUnitIdsLoader;
import com.blackmorse.hattrick.api.leaguedetails.model.LeagueDetails;
import com.blackmorse.hattrick.api.leaguefixtures.model.LeagueFixtures;
import com.blackmorse.hattrick.api.search.model.Result;
import com.blackmorse.hattrick.api.teamdetails.model.TeamDetails;
import com.blackmorse.hattrick.model.TeamWithMatchAndPlayers;
import com.blackmorse.hattrick.model.TeamWithMatchDetails;
import com.blackmorse.hattrick.model.enums.MatchType;
import com.blackmorse.hattrick.model.LeagueUnit;
import com.blackmorse.hattrick.model.LeagueUnitId;
import com.blackmorse.hattrick.model.Match;
import com.blackmorse.hattrick.model.Team;
import com.blackmorse.hattrick.model.TeamWithMatch;
import com.blackmorse.hattrick.model.TeamWithMatches;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Slf4j
public class HattrickService {
    private final Scheduler scheduler;
    private final Hattrick hattrick;
    private final LeagueUnitIdsLoader leagueUnitIdsLoader;
    private final AtomicLong leagueUnitCounter = new AtomicLong();
    private final AtomicLong teamsCounter  = new AtomicLong();
    private final AtomicLong matchDetailsCounter  = new AtomicLong();
    private final AtomicLong teamsWithPlayersCounter = new AtomicLong();

    public HattrickService(@Qualifier("apiExecutor") ExecutorService executorService,
                           Hattrick hattrick,
                           LeagueUnitIdsLoader leagueUnitIdsLoader) {
        this.scheduler = io.reactivex.schedulers.Schedulers.from(executorService);
        this.hattrick = hattrick;
        this.leagueUnitIdsLoader = leagueUnitIdsLoader;
    }

    public List<LeagueUnitId> getAllLeagueUnitIdsForCountry(List<String> countryNames) {
        return leagueUnitIdsLoader.load(countryNames);
    }


    public List<TeamWithMatches> getAllTeamsWithMatches(List<LeagueUnitId> leagueUnitIds, Integer season) {
        return Flowable.fromIterable(leagueUnitIds)
                .parallel()
                .runOn(scheduler)
                .flatMap(leagueUnitId -> {
                    Integer offset = leagueUnitId.getLeague().getSeasonOffset();
                    Integer offsettedSeason = season + offset;
                    LeagueFixtures leagueFixtures = hattrick.leagueUnitFixturesById(leagueUnitId.getId(), offsettedSeason);

                    log.info("LeagueFixture: {}", leagueUnitCounter.incrementAndGet());

                    LeagueUnit leagueUnit = //new SeasonHistoryLoader.LeagueUnit(leagueUnitId.getLeague(), leagueFixtures.getLeagueLevelUnitId(), leagueFixtures.getLeagueLevelUnitName());
                            LeagueUnit.builder()
                                    .league(leagueUnitId.getLeague())
                                    .id(leagueFixtures.getLeagueLevelUnitId())
                                    .name(leagueFixtures.getLeagueLevelUnitName())
                                    .level(leagueLevelFromName(leagueFixtures.getLeagueLevelUnitName()))
                                    .build();

                    Map<Team, List<Match>> teamMatchMap = new HashMap<>();

                    for (com.blackmorse.hattrick.api.leaguefixtures.model.Match match : leagueFixtures.getMatches()) {
                        if (season.equals(hattrick.getSeason()) && leagueUnit.getLeague().getNextRound() <= match.getMatchRound()) continue;

                        Team homeTeam = Team.builder()
                                .leagueUnit(leagueUnit)
                                .id(match.getHomeTeam().getHomeTeamId())
                                .name(match.getHomeTeam().getHomeTeamName())
                            .build();

                        teamMatchMap.computeIfAbsent(homeTeam, team -> new ArrayList<>())//.add(new SeasonHistoryLoader.Match(match.getMatchId(), match.getMatchRound(), match.getMatchDate()));
                            .add(Match.builder()
                                    .id(match.getMatchId())
                                    .round(match.getMatchRound())
                                    .date(match.getMatchDate())
                                    .season(season)
                                    .build());
                        Team awayTeam = Team.builder()
                                .leagueUnit(leagueUnit)
                                .id(match.getAwayTeam().getAwayTeamId())
                                .name(match.getAwayTeam().getAwayTeamName())
                            .build();
                        teamMatchMap.computeIfAbsent(awayTeam, team -> new ArrayList<>())//.add(SeasonHistoryLoader.Match(match.getMatchId(), match.getMatchRound(), match.getMatchDate()));
                                .add(Match.builder()
                                        .id(match.getMatchId())
                                        .round(match.getMatchRound())
                                        .date(match.getMatchDate())
                                        .season(season)
                                        .build());
                    }

                    return Flowable.fromIterable(teamMatchMap.entrySet().stream()
                            .map(entry -> TeamWithMatches.builder().team(entry.getKey()).matches(entry.getValue()).build())
                            .collect(Collectors.toList()));
                }).filter(teamWithMatches -> {

                    TeamDetails teamDetails = hattrick.teamDetails(teamWithMatches.getTeam().getId());

                    log.info("{} teamDetails {} is OK: {}", teamsCounter.incrementAndGet(), teamWithMatches.getTeam().getLeagueUnit().getName(), teamDetails.getUser().getUserId() != 0L);
                    return teamDetails.getUser().getUserId() != 0L;
                })
                .sequential()
                .toList()
                .blockingGet();
    }

    @Data
    @Builder
    private static class StringAndNumber {
        private final String string;
        private final Integer number;
    }

    private static Integer leagueLevelFromName(String name) {
        if(!name.contains(".")) return 1;
        else {
            return Hattrick.romansToArab.get(name.split("\\.")[0]);
        }
    }

    public List<TeamWithMatchDetails> getMatchDetails(List<TeamWithMatches> teamWMatches) {
        return Flowable.fromIterable(teamWMatches)
                .flatMap(teamWithMatches -> Flowable.fromIterable(
                        teamWithMatches.getMatches().stream()
                                .map(match -> TeamWithMatch.builder().team(teamWithMatches.getTeam()).match(match).build())
                                .collect(Collectors.toList())
                ))
                .parallel()
                .runOn(scheduler)
                .map(teamWithMatch -> {
                    com.blackmorse.hattrick.api.matchdetails.model.MatchDetails matchDetails = hattrick.getMatchDetails(teamWithMatch.getMatch().getId());
                    log.info("Match {}", matchDetailsCounter.incrementAndGet());
                    return TeamWithMatchDetails.builder().teamWithMatch(teamWithMatch).matchDetails(matchDetails).build();
                })
                .sequential()
                .toList()
                .blockingGet();
    }

    public List<TeamWithMatchDetails> getLastMatchDetails(List<LeagueUnitId> leagueUnitIds) {
        return Flowable.fromIterable(leagueUnitIds)
                .parallel()
                .runOn(scheduler)
                .flatMap(leagueUnitId -> {
                        LeagueDetails leagueDetails = hattrick.getLeagueUnitById(leagueUnitId.getId());

                        LeagueUnit leagueUnit = LeagueUnit.builder()
                                .league(leagueUnitId.getLeague())
                                .id(leagueUnitId.getId())
                                .name(leagueDetails.getLeagueLevelUnitName())
                                .level(leagueDetails.getLeagueLevel())
                                .build();

                        log.info("League details: {}", leagueUnitCounter.incrementAndGet());
                        if (leagueDetails.getTeams() == null) {
                            return Flowable.empty();
                        }
                        return Flowable.fromIterable(leagueDetails.getTeams().stream()
                                .filter(team -> team.getUserId() != 0L)
                                .map(team -> Team.builder()
                                        .leagueUnit(leagueUnit)
                                        .id(team.getTeamId())
                                        .name(team.getTeamName())
                                        .build()).collect(Collectors.toList()));
                })
                .map(team -> {
                    log.info("teams: {}", teamsCounter.incrementAndGet());
                    List<com.blackmorse.hattrick.api.matchesarchive.model.Match> matchList = hattrick.getCurrentSeasonMatches(team.getId()).getTeam().getMatchList();

                    Match match = null;

                    if(matchList != null) {
                        match = matchList.stream()
                            .filter(m -> m.getMatchType().equals(MatchType.LEAGUE_MATCH))
                            .max(Comparator.comparing(com.blackmorse.hattrick.api.matchesarchive.model.Match::getMatchDate))
                            .map(m -> Match.builder()
                                .id(m.getMatchId())
                                .round(team.getLeagueUnit().getLeague().getNextRound() - 1)
                                .date(m.getMatchDate())
                                .season(hattrick.getSeason())
                                .build())
                        .orElse(null);
                    }

            return TeamWithMatch.builder()
                    .team(team)
                    .match(match)
                    .build();
        })
                .filter(teamWithMatch -> teamWithMatch.getMatch() != null)
                .map(teamWithMatch -> {
                    log.info("match details: {}", matchDetailsCounter.incrementAndGet());
                    com.blackmorse.hattrick.api.matchdetails.model.MatchDetails matchDetails = hattrick.getMatchDetails(teamWithMatch.getMatch().getId());
                    return TeamWithMatchDetails.builder().teamWithMatch(teamWithMatch).matchDetails(matchDetails).build();
                }).sequential()
                .toList()
            .blockingGet();
    }

    public List<TeamWithMatchAndPlayers> getPlayersFromTeam(List<TeamWithMatchDetails> teamWithMatchDetails) {
        return Flowable.fromIterable(teamWithMatchDetails)
                .parallel()
                .runOn(scheduler)
                .map(teamWithMatchDetail -> {
                    log.info("Players for teams: {}", teamsWithPlayersCounter.incrementAndGet());
                    return new TeamWithMatchAndPlayers(teamWithMatchDetail.getTeamWithMatch(),
                            hattrick.getPlayersFromTeam(teamWithMatchDetail.getTeamWithMatch().getTeam().getId()));
                })
                .sequential()
                .toList()
            .blockingGet();
    }

    public void shutDown() {
        scheduler.shutdown();
    }
}