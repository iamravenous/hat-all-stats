package databases.requests

import databases.{RestClickhouseDAO, SqlBuilder}

import scala.concurrent.Future

trait ClickhouseOverviewRequest[T] extends ClickhouseRequest[T] {
  val sql: String
  val limit = 5

  def execute(season: Int, round: Int, leagueId: Option[Int], divisionLevel: Option[Int])
             (implicit restClickhouseDAO: RestClickhouseDAO): Future[List[T]] = {

    val builder = SqlBuilder(sql)
      .where
        .round(round)
        .season(season)
        .leagueId(leagueId)
        .divisionLevel(divisionLevel)
      .and
        .page(0)
        .pageSize(limit)

    restClickhouseDAO.execute(builder.build, rowParser)
  }
}
