package databases.requests.playerstats.player

import anorm.RowParser
import databases.requests.ClickhouseRequest
import databases.requests.model.player.PlayerRating

object PlayerRatingsRequest extends ClickhousePlayerRequest[PlayerRating]{
  override val sortingColumns: Seq[String] = Seq("age", "rating", "rating_end_of_match")
  override val oneRoundSql: String = s"""
         |SELECT
         |    team_name,
         |    team_id,
         |    league_unit_name,
         |    league_unit_id,
         |    player_id,
         |    first_name,
         |    last_name,
         |    ((age * 112) + days)  AS age,
         |    rating,
         |    rating_end_of_match,
         |    nationality,
         |    ${ClickhouseRequest.roleIdCase("role_id")} as role
         |FROM hattrick.player_stats
         |__where__ AND (round = __round__)
         |ORDER BY
         |    __sortBy__ __sortingDirection__, player_id __sortingDirection__
         |__limit__""".stripMargin
  override val rowParser: RowParser[PlayerRating] = PlayerRating.mapper
}
