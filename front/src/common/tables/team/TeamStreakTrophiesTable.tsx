import React from 'react'
import ModelTable, { ModelTablePropsWrapper, SortingState, ModelTableProps } from '../../ModelTable';
import LevelData from '../../../rest/models/LevelData';
import TeamStreakTrophies from '../../../rest/models/team/TeamStreakTrophies';
import { StatsTypeEnum } from '../../../rest/StatisticsParameters';
import { getTeamStreakTrophies } from '../../../rest/Client';
import '../../../i18n'
import { Translation } from 'react-i18next'
import ModelTableTh from '../../ModelTableTh'
import LeagueUnitLink from '../../links/LeagueUnitLink';
import TeamLink from '../../links/TeamLink'

abstract class TeamStreakTrophiesTable<Data extends LevelData, TableProps extends ModelTableProps<Data>>
    extends ModelTable<Data, TableProps, TeamStreakTrophies> {

    constructor(props: ModelTablePropsWrapper<Data, TableProps>) {
        super(props, 'trophies_number', {statType: StatsTypeEnum.ROUND, roundNumber: props.modelTableProps.currentRound()},
            [StatsTypeEnum.ROUND])
    }

    fetchDataFunction = getTeamStreakTrophies

    columnHeaders(sortingState: SortingState): JSX.Element {
        return <Translation>
            {
            (t, { i18n }) =>
            <tr>
                <th className="position hint" popped-hint={t('table.position')}>{t('table.position_abbr')}</th>
                <th>{t('table.team')}</th>
                <th className="value">{t('table.league')}</th>
                <ModelTableTh title='table.trophies' sortingField='trophies_number' sortingState={sortingState} />
                <ModelTableTh title='table.victories' sortingField='number_of_victories' sortingState={sortingState} />
                <ModelTableTh title='table.undefeated' sortingField='number_of_undefeated' sortingState={sortingState} />
            </tr>
        }
        </Translation>
    }

    columnValues(index: number, teamStreakTrophies: TeamStreakTrophies): JSX.Element {
        let teamSortingKey = teamStreakTrophies.teamSortingKey
        return <tr key={"team_streak_trophies_row_" + index}>
            <td>{index + 1}</td>
            <td><TeamLink id={teamSortingKey.teamId} name={teamSortingKey.teamName} /></td>
            <td className="value"><LeagueUnitLink id={teamSortingKey.leagueUnitId} name={teamSortingKey.leagueUnitName}/></td>
            <td className="value">{teamStreakTrophies.trophiesNumber}</td>
            <td className="value">{teamStreakTrophies.numberOfVictories}</td>
            <td className="value">{teamStreakTrophies.numberOfUndefeated}</td>
        </tr>
    }
}

export default TeamStreakTrophiesTable