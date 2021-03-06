package chpp.leaguefixtures.models

import java.util.Date

import cats.syntax.all._
import chpp.BaseXmlMapper
import chpp.matchesarchive.models.{AwayTeam, HomeTeam}
import com.lucidchart.open.xtract.XmlReader._
import com.lucidchart.open.xtract.{XmlReader, __}

case class Match(matchId: Long,
                 matchRound: Int,
                 homeTeam: HomeTeam,
                 awayTeam: AwayTeam,
                 matchDate: Date,
                 homeGoals: Int,
                 awayGoals: Int)

object Match extends BaseXmlMapper {
  implicit val reader: XmlReader[Match] = (
    (__ \ "MatchID").read[Long],
    (__ \ "MatchRound").read[Int],
    (__ \ "HomeTeam").read[HomeTeam],
    (__ \ "AwayTeam").read[AwayTeam],
    (__ \ "MatchDate").read[String].map(date),
    (__ \ "HomeGoals").read[Int],
    (__ \ "AwayGoals").read[Int],
    ).mapN(apply _)
}
