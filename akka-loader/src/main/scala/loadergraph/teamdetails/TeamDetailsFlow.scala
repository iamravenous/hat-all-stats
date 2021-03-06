package loadergraph.teamdetails

import akka.actor.ActorSystem
import akka.stream.scaladsl.Flow
import chpp.OauthTokens
import chpp.teamdetails.{TeamDetailsHttpFlow, TeamDetailsRequest}
import flows.LogProgressFlow
import models.clickhouse.TeamDetailsModelCH
import models.stream.StreamMatchDetails

import scala.concurrent.ExecutionContext

object TeamDetailsFlow {
  def apply()(implicit oauthTokens: OauthTokens, system: ActorSystem,
              executionContext: ExecutionContext): Flow[StreamMatchDetails, TeamDetailsModelCH, _] = {
    Flow[StreamMatchDetails]
      .map(matchDetails => (TeamDetailsRequest(teamId = Some(matchDetails.matc.team.id), includeFlags = Some(true), includeDomesticFlags = Some(true)), matchDetails))
      .async
      .via(TeamDetailsHttpFlow())
      .via(LogProgressFlow("Team Details", Some(_._2.matc.team.leagueUnit.league.activeTeams)))
      .map{case(teamDetails, matchDetails) =>
        val teams = teamDetails.teams
          .filter(t => t.teamId == matchDetails.matc.team.id)

        val team = teams
          .head
        TeamDetailsModelCH.convert(team, matchDetails)
      }
  }
}
