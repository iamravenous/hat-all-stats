import LevelData from '../../../rest/models/LevelData';
import ModelTable, { ModelTablePropsWrapper, SortingState, ModelTableProps } from '../../ModelTable';
import React from 'react';
import MatchTopHatstats from '../../../rest/models/match/MatchTopHatstats';
import { StatsTypeEnum } from '../../../rest/StatisticsParameters';
import '../../../i18n'
import { Translation } from 'react-i18next'
import ModelTableTh from '../../ModelTableTh'
import TeamLink from '../../links/TeamLink'
import LeagueUnitLink from '../../links/LeagueUnitLink'
import { getMatchesTopHatstats } from '../../../rest/Client';

abstract class MatchTopHatstatsTable<Data extends LevelData, TableProps extends ModelTableProps<Data>>
    extends ModelTable<Data, TableProps, MatchTopHatstats> {
    
    constructor(props: ModelTablePropsWrapper<Data, TableProps>) {
        super(props, 'sum_hatstats', {statType: StatsTypeEnum.ACCUMULATE},
            [StatsTypeEnum.ACCUMULATE, StatsTypeEnum.ROUND])
    }

    fetchDataFunction = getMatchesTopHatstats

    columnHeaders(sortingState: SortingState): JSX.Element {
        return <Translation>
            {
            (t, { i18n }) =>
            <tr>
                <th className="position hint" popped-hint={t('table.position')}>{t('table.position_abbr')}</th>
                <th className="value">{t('table.league')}</th>
                <th>{t('table.team')}</th>
                <ModelTableTh title='table.hatstats' sortingField='sum_hatstats' sortingState={sortingState} />
                <th></th>
                <ModelTableTh title='table.hatstats' sortingField='sum_hatstats' sortingState={sortingState} />
                <th>{t('table.team')}</th>
            </tr>
            }
        </Translation>
    }

    columnValues(index: number, matchHatstats: MatchTopHatstats): JSX.Element {
        return <tr key={"top_matches_row" + index}>
            <td>{index + 1}</td>
            <td className="value"><LeagueUnitLink id={matchHatstats.homeTeam.leagueUnitId} name={matchHatstats.homeTeam.leagueUnitName} /></td>
            <td className="value"><TeamLink id={matchHatstats.homeTeam.teamId} name={matchHatstats.homeTeam.teamName} /></td>
            <td className="value">{matchHatstats.homeHatstats}</td>
            <td className="value">{matchHatstats.homeGoals} : {matchHatstats.awayGoals}</td>
            <td className="value">{matchHatstats.awayHatstats}</td>
            <td className="value"><TeamLink id={matchHatstats.awayTeam.teamId} name={matchHatstats.awayTeam.teamName} /></td>           
        </tr>
    }
}

export default MatchTopHatstatsTable