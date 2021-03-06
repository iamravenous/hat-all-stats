package loadergraph.teams

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import chpp.OauthTokens
import chpp.worlddetails.{WorldDetailsHttpFlow, WorldDetailsRequest}
import models.stream.League

import scala.concurrent.ExecutionContext

object LeagueWithLevelSource {
  def apply(leagueId: Int)
            (implicit oauthTokens: OauthTokens, system: ActorSystem,
             executionContext: ExecutionContext) = {
    Source.single(leagueId)
      .map(leagueId => (WorldDetailsRequest(leagueId = Some(leagueId)), leagueId))
      .via(WorldDetailsHttpFlow())
      .map(_._1)
      .flatMapConcat(worldDetails => {
        val league = worldDetails.leagueList.head
        Source(
          (1 to worldDetails.leagueList.head.numberOfLevels)
            .map(level => LeagueWithLevel(league = League(leagueId = league.leagueId,
                seasonOffset = league.seasonOffset,
                nextRound = league.matchRound,
                season = league.season - league.seasonOffset,
                activeTeams = league.activeTeams),
              level = level))
        )})
  }
}
