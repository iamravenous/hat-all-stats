package databases.requests.matchdetails

import anorm.RowParser
import databases.requests.ClickhouseStatisticsRequest
import models.clickhouse.LeagueUnitRating

object LeagueUnitHatstatsRequest extends ClickhouseStatisticsRequest[LeagueUnitRating] {
  override val sortingColumns: Seq[String] = Seq("hatstats", "midfield", "defense", "attack")

  override val rowParser: RowParser[LeagueUnitRating] = LeagueUnitRating.leagueUnitRatingMapper

  override val aggregateSql: String =
    """select league_unit_id,
          |league_unit_name,
          |toInt32(__func__(hatstats)) as hatstats,
          |toInt32(__func__(midfield)) as midfield,
          |toInt32(__func__(defense)) as defense,
          |toInt32(__func__(attack)) as attack
          | from
          |   (select league_unit_id,
          |     league_unit_name,
          |     round,
          |     toInt32(avg(rating_midfield * 3 + rating_right_def + rating_left_def + rating_mid_def + rating_right_att + rating_mid_att + rating_left_att)) as hatstats,
          |     toInt32(avg(rating_midfield)) as midfield,
          |     toInt32(avg((rating_right_def + rating_left_def + rating_mid_def))) as defense,
          |     toInt32(avg((rating_right_att + rating_mid_att + rating_left_att))) as attack
          |     from hattrick.match_details
          |     __where__ and rating_midfield + rating_right_def + rating_left_def + rating_mid_def + rating_right_att + rating_mid_att + rating_left_att != 0
          |     group by league_unit_id, league_unit_name, round)
          |group by league_unit_id, league_unit_name order by __sortBy__ __sortingDirection__, league_unit_id desc __limit__""".stripMargin

  override val oneRoundSql: String =
        """select league_unit_id,
         |     league_unit_name,
         |     toInt32(avg(rating_midfield * 3 + rating_right_def + rating_left_def + rating_mid_def + rating_right_att + rating_mid_att + rating_left_att)) as hatstats,
         |     toInt32(avg(rating_midfield)) as midfield,
         |     toInt32(avg((rating_right_def + rating_left_def + rating_mid_def))) as defense,
         |     toInt32(avg((rating_right_att + rating_mid_att + rating_left_att))) as attack
         |     from hattrick.match_details
         |     __where__ AND round = __round__ AND rating_midfield + rating_right_def + rating_left_def + rating_mid_def + rating_right_att + rating_mid_att + rating_left_att != 0
         |     group by league_unit_id, league_unit_name
         | order by __sortBy__ __sortingDirection__, league_unit_id __sortingDirection__ __limit__""".stripMargin
}
