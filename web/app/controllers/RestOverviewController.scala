package controllers

import java.util.Date

import databases.RestClickhouseDAO
import io.swagger.annotations.Api
import javax.inject.Inject
import models.web.rest.LevelData
import models.web.rest.LevelData.Rounds
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import service.RestOverviewStatsService
import service.leagueinfo.{Finished, LeagueInfoService, Loading, Scheduled}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class WorldLoadingInfo(proceedCountries: Int,
                            nextCountry: Option[(Int, String, Date)],
                            currentCountry: Option[(Int, String)])

object WorldLoadingInfo {
  implicit val writes = Json.writes[WorldLoadingInfo]
}

case class WorldData(countries: Seq[(Int, String)],
                     seasonOffset: Int,
                     seasonRoundInfo: Seq[(Int, Rounds)],
                     currency: String,
                     currencyRate: Double,
                     loadingInfo: Option[WorldLoadingInfo],
                     isWorldData: String /*TODO for detecting type at TS*/) extends LevelData

object WorldData {
  implicit val writes = Json.writes[WorldData]
}

@Api(produces = "application/json")
class RestOverviewController @Inject()(val controllerComponents: ControllerComponents,
                                       implicit val restClickhouseDAO: RestClickhouseDAO,
                                       val restOverviewStatsService: RestOverviewStatsService,
                                       val leagueInfoService: LeagueInfoService)
                                  extends RestController {
  private val lastLeagueId = 100

  def getWorldData() = Action.async{implicit request =>
    val countries = leagueInfoService.idToStringCountryMap

    val leagueInfoCountries = leagueInfoService.leagueInfo.leagueInfo.values.toSeq
    val proceedCountries = leagueInfoCountries.count(_.loadingInfo == Finished)

    val worldLoadingInfo = if(proceedCountries == leagueInfoService.leagueInfo.leagueInfo.size) {
      None
    } else {
      val nextCountry = leagueInfoCountries.filter(_.loadingInfo match {
        case Scheduled(_) => true
        case _ => false
      }).sortBy(_.loadingInfo.asInstanceOf[Scheduled].date).headOption
        .map(leagueInfo => (leagueInfo.leagueId, leagueInfo.league.getEnglishName, leagueInfo.loadingInfo.asInstanceOf[Scheduled].date))

      val currentCountry = leagueInfoCountries.find(_.loadingInfo match {
        case Loading => true
        case _ => false
      })
        .map(leagueInfo => (leagueInfo.leagueId, leagueInfo.league.getEnglishName))

      Some(WorldLoadingInfo(proceedCountries, nextCountry, currentCountry))
    }

    val worldData = WorldData(countries = countries,
      seasonOffset = 0,
      seasonRoundInfo = leagueInfoService.leagueInfo.seasonRoundInfo(lastLeagueId),
      currency = "$",
      currencyRate = 10.0d,
      worldLoadingInfo,
      isWorldData = "true"
      )

    Future(Ok(Json.toJson(worldData)))
  }

  def numberOverview(season: Int, round: Int, leagueId: Option[Int], divisionLevel: Option[Int]) = Action.async { implicit request =>
    restOverviewStatsService.numberOverview(season, round, leagueId, divisionLevel)
      .map(numberOverview => Ok(Json.toJson(numberOverview)))
  }

  def formations(season: Int, round: Int, leagueId: Option[Int], divisionLevel: Option[Int]) = Action.async{ implicit request =>
    restOverviewStatsService.formations(season, round, leagueId, divisionLevel)
      .map(formations => Ok(Json.toJson(formations)))
  }

  def averagesOverview(season: Int, round: Int, leagueId: Option[Int], divisionLevel: Option[Int]) = Action.async { implicit request =>
    restOverviewStatsService.averageOverview(season, round, leagueId, divisionLevel)
      .map(averages => Ok(Json.toJson(averages)))
  }

  def surprisingMatches(season: Int, round: Int, leagueId: Option[Int], divisionLevel: Option[Int]) = Action.async {implicit request =>
    restOverviewStatsService.surprisingMatches(season, round, leagueId, divisionLevel)
      .map(matches => Ok(Json.toJson(matches)))
  }

  def topHatstatsTeams(season: Int, round: Int, leagueId: Option[Int], divisionLevel: Option[Int]) = Action.async {implicit request =>
    restOverviewStatsService.topHatstatsTeams(season, round, leagueId, divisionLevel)
      .map(teams => Ok(Json.toJson(teams)))
  }

  def topSalaryTeams(season: Int, round: Int, leagueId: Option[Int], divisionLevel: Option[Int]) = Action.async{implicit request =>
    restOverviewStatsService.topSalaryTeams(season, round, leagueId, divisionLevel)
      .map(teams => Ok(Json.toJson(teams)))
  }

  def topMatches(season: Int, round: Int, leagueId: Option[Int], divisionLevel: Option[Int]) = Action.async{implicit request =>
    restOverviewStatsService.topMatches(season, round, leagueId, divisionLevel)
      .map(matches => Ok(Json.toJson(matches)))
  }

  def topSalaryPlayers(season: Int, round: Int, leagueId: Option[Int], divisionLevel: Option[Int]) = Action.async{implicit request =>
    restOverviewStatsService.topSalaryPlayers(season, round, leagueId, divisionLevel)
      .map(players => Ok(Json.toJson(players)))
  }

  def topRatingPlayers(season: Int, round: Int, leagueId: Option[Int], divisionLevel: Option[Int]) = Action.async { implicit request =>
    restOverviewStatsService.topRatingPlayers(season, round, leagueId, divisionLevel)
      .map(players => Ok(Json.toJson(players)))
  }

  def topMatchAttendance(season: Int, round: Int, leagueId: Option[Int], divisionLevel: Option[Int]) = Action.async{ implicit request =>
    restOverviewStatsService.topMatchAttendance(season, round, leagueId, divisionLevel)
      .map(matches => Ok(Json.toJson(matches)))
  }

  def topTeamVictories(season: Int, round: Int, leagueId: Option[Int], divisionLevel: Option[Int]) = Action.async{ implicit request =>
    restOverviewStatsService.topTeamVictories(season, round, leagueId, divisionLevel)
      .map(teams => Ok(Json.toJson(teams)))
  }

  def topSeasonScorers(season: Int, round: Int, leagueId: Option[Int], divisionLevel: Option[Int]) = Action.async{implicit request =>
    restOverviewStatsService.topSeasonScorers(season, round, leagueId, divisionLevel)
      .map(players => Ok(Json.toJson(players)))
  }

  def totalOverview(season: Int, round: Int, leagueId: Option[Int], divisionLevel: Option[Int]) = Action.async { implicit request =>
    restOverviewStatsService.totalOverview(season, round, leagueId, divisionLevel)
      .map(totalOverview => Ok(Json.toJson(totalOverview)))
  }
}
