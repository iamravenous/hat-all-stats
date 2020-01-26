package controllers

import databases.ClickhouseDAO
import hattrick.Hattrick
import javax.inject.{Inject, Singleton}
import models.web.{AbstractWebDetails, SeasonInfo, WebPagedEntities}
import play.api.mvc.{BaseController, ControllerComponents}
import service.DefaultService
import utils.Romans

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class WebDivisionLevelDetails(leagueName: String, leagueId: Int, seasonInfo: SeasonInfo, divisionLevel: Int, divisionLevelRoman: String,
                                   leagueUnitLinks: Seq[(String, String)]) extends AbstractWebDetails

@Singleton
class DivisionLevelController@Inject() (val controllerComponents: ControllerComponents,
                                        val clickhouseDAO: ClickhouseDAO,
                                        val defaultService: DefaultService,
                                        val hattrick: Hattrick) extends BaseController {
  def bestTeams(leagueId: Int, season: Int, divisionLevel: Int, page: Int) = Action.async{ implicit request =>
    val leagueName = defaultService.leagueIdToCountryNameMap(leagueId).getEnglishName

    val seasonFunction: Int => String = s => routes.DivisionLevelController.bestTeams(leagueId, s, divisionLevel, 0).url
    val seasonInfo = SeasonInfo(season, defaultService.seasonsWithLinks(leagueId, seasonFunction))

    val pageUrlFunc: Int => String = p => routes.DivisionLevelController.bestTeams(leagueId, season, divisionLevel, p).url

    val leagueUnitIdFuture = Future(hattrick.api.search().searchLeagueId(leagueId).searchType(3).searchString(Romans(divisionLevel) + "." + "1")
      .execute().getSearchResults.get(0).getResultId)

    clickhouseDAO.bestTeams(leagueId = Some(leagueId), season = Some(season), divisionLevel = Some(divisionLevel), page = page)
      .zipWith(leagueUnitIdFuture){case (bestTeams, leagueUnitId) =>
        val details = WebDivisionLevelDetails(leagueName, leagueId, seasonInfo, divisionLevel, Romans(divisionLevel),
          leagueUnitNumbers(season, divisionLevel, leagueUnitId))

        Ok(views.html.divisionlevel.bestTeams(details, WebPagedEntities(bestTeams, page, pageUrlFunc)))
      }
  }

  def bestLeagueUnits(leagueId: Int, season: Int, divisionLevel: Int, page: Int) = Action.async{  implicit request =>
    val leagueName = defaultService.leagueIdToCountryNameMap(leagueId).getEnglishName

    val seasonFunction: Int => String = s => routes.DivisionLevelController.bestLeagueUnits(leagueId, s, divisionLevel, 0).url
    val seasonInfo = SeasonInfo(season, defaultService.seasonsWithLinks(leagueId, seasonFunction))



    val pageUrlFunc: Int => String = p => routes.DivisionLevelController.bestLeagueUnits(leagueId, season, divisionLevel, p).url

    val leagueUnitIdFuture = Future(hattrick.api.search().searchLeagueId(leagueId).searchType(3).searchString(Romans(divisionLevel) + "." + "1")
      .execute().getSearchResults.get(0).getResultId)

    clickhouseDAO.bestLeagueUnits(leagueId = Some(leagueId), season = Some(season), divisionLevel = Some(divisionLevel), page = page)
      .zipWith(leagueUnitIdFuture){case (bestLeagueUnits, leagueUnitId) =>

        val details = WebDivisionLevelDetails(leagueName, leagueId, seasonInfo, divisionLevel, Romans(divisionLevel),
          leagueUnitNumbers(season, divisionLevel, leagueUnitId))

        Ok(views.html.divisionlevel.bestLeagueUnits(details, WebPagedEntities(bestLeagueUnits, page, pageUrlFunc)))
      }
  }

  def leagueUnitNumbers(season: Int, divisionLevel: Int, baseLeagueUnitId: Long): Seq[(String, String)] =
    defaultService.leagueNumbersMap(divisionLevel).map(number =>
      number.toString -> routes.LeagueUnitController.bestTeams(baseLeagueUnitId + number - 1, season, 0).url)
}