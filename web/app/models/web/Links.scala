package models.web

case class Links(seasonLinks: SeasonLinks,
                 statTypeLinks: StatTypeLinks,
                 sortByLinks: SortByLinks,
                 pageSizeLinks: PageSizeLinks,
                 sortingDirectionLinks: SortingDirectionLinks)

case class SeasonLinks(season: Int, allSeasons: Seq[(String, String)])

case class StatTypeLinks(links: Seq[(String, String)], currentStatType: StatsType)

case class PageSizeLinks(links: Seq[(String, String)], currentPageSize: Int)

case class SortingDirectionLinks(links: Seq[(String, String)], currentDirection: SortingDirection)

object SeasonLinks {
  def apply(currentSeason: Int, seasonLinkFunction: Int => String,
            seasons: Seq[Int], seasonOffset: Int): SeasonLinks = {
    SeasonLinks(currentSeason + seasonOffset, seasons.map(season => ((season + seasonOffset).toString, seasonLinkFunction(season))))
  }
}

object StatTypeLinks {
  def withAverages(statTypeUrlFunc: StatsType => String, rounds: Seq[Int], current: StatsType): StatTypeLinks = {
    val seq = Seq(Avg.function -> statTypeUrlFunc(Avg), Max.function -> statTypeUrlFunc(Max)) ++
      rounds.map(roundNumber => (roundNumber.toString, statTypeUrlFunc(Round(roundNumber))))

    StatTypeLinks(seq, current)
  }

  def withAccumulator(statTypeUrlFunc: StatsType => String, rounds: Seq[Int], current: StatsType): StatTypeLinks = {
    val seq = rounds.map(roundNumber => (roundNumber.toString, statTypeUrlFunc(Round(roundNumber)))) ++ Seq(("all", statTypeUrlFunc(Accumulate)))
    StatTypeLinks(seq, current)
  }

  def onlyRounds(statTypeUrlFunc: StatsType => String, rounds: Seq[Int], current: StatsType): StatTypeLinks = {
    val seq = rounds.map(roundNumber => (roundNumber.toString, statTypeUrlFunc(Round(roundNumber))))
    StatTypeLinks(seq, current)
  }
}

case class SortLink(columnName: String, localizationKey: String, link: String)

case class SortByLinks(links: Seq[SortLink], currentSort: String)

object SortByLinks {
  def apply(sortByFunc: String => String, columns: Seq[(String, String)], currentSort: String): SortByLinks = {
    SortByLinks(columns.map{case(column, localizationKey) =>
      SortLink(column, localizationKey, sortByFunc(column))}, currentSort)
  }
}

object PageSizeLinks {
  private val pageSizes = Seq(16, 32, 64)

  def apply(pageSizeFunc: Int => String, currentPageSize: Int): PageSizeLinks = {
    PageSizeLinks(pageSizes.map(ps => (ps.toString, pageSizeFunc(ps))), currentPageSize)
  }
}

object SortingDirectionLinks {
  def apply(sortingDirectionFunc: SortingDirection => String,
            currentSortingDirection: SortingDirection): SortingDirectionLinks = {
    val seq = Seq((Asc.toString, sortingDirectionFunc(Asc)), (Desc.toString, sortingDirectionFunc(Desc)))
    SortingDirectionLinks(seq, currentSortingDirection)
  }
}