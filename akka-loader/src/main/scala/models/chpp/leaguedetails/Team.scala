package models.chpp.leaguedetails

import com.lucidchart.open.xtract.{XmlReader, __}
import models.chpp.BaseXmlMapper
import cats.syntax.all._

case class Team(userId: Int,
                teamId: Int,
                teamName: String,
                position: Int,
                positionChange: Int,
                matches: Int,
                goalsFor: Int,
                goalsAgainst: Int,
                points: Int,
                won: Int,
                draws: Int,
                lost: Int)

object Team extends BaseXmlMapper {
  implicit val reader: XmlReader[Team] = (
    (__ \ "UserId").read[Int],
    (__ \ "TeamID").read[Int],
    (__ \ "TeamName").read[String],
    (__ \ "Position").read[Int],
    (__ \ "PositionChange").read[Int],
    (__ \ "Matches").read[Int],
    (__ \ "GoalsFor").read[Int],
    (__ \ "GoalsAgainst").read[Int],
    (__ \ "Points").read[Int],
    (__ \ "Won").read[Int],
    (__ \ "Draws").read[Int],
    (__ \ "Lost").read[Int],
    ).mapN(apply _)
}