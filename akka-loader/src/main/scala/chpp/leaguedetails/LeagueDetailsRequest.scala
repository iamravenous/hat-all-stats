package chpp.leaguedetails

import akka.http.scaladsl.model.HttpRequest
import chpp.{AbstractRequest, OauthTokens, RequestCreator}

case class LeagueDetailsRequest(leagueUnitId: Option[Int]) extends AbstractRequest {
  override def createRequest()(implicit oauthTokens: OauthTokens): HttpRequest = {
    val map = RequestCreator.params("leaguedetails", "1.5",
      "leagueLevelUnitID" -> leagueUnitId)

    RequestCreator.create(map)
  }
}
