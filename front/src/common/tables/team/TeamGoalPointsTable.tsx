import React from 'react';
import LevelData from '../../../rest/models/leveldata/LevelData';
import AbstractTableSection, { SortingState, DataRequest } from '../AbstractTableSection';
import LevelDataProps, { LevelDataPropsWrapper } from '../../LevelDataProps'
import '../../../i18n'
import { Translation } from 'react-i18next'
import { StatsTypeEnum } from '../../../rest/models/StatisticsParameters';
import { getTeamGoalPoints } from '../../../rest/Client';
import ModelTableTh from '../../../common/elements/SortingTableTh'
import LeagueUnitLink from '../../links/LeagueUnitLink';
import TeamLink from '../../links/TeamLink'
import TeamGoalPoints from '../../../rest/models/team/TeamGoalPoints';
import { SelectorsEnum } from '../SelectorsEnum';
import { LoadingEnum } from '../../enums/LoadingEnum'
import RestTableData from '../../../rest/models/RestTableData';

class TeamGoalPointsTable<Data extends LevelData, TableProps extends LevelDataProps<Data>> 
    extends AbstractTableSection<Data, TableProps, TeamGoalPoints> {
    

    constructor(props: LevelDataPropsWrapper<Data, TableProps>) {
        super(props, 'points', {statType: StatsTypeEnum.ROUND, roundNumber: props.levelDataProps.currentRound()},
            [StatsTypeEnum.ROUND],
            [SelectorsEnum.SEASON_SELECTOR, SelectorsEnum.STATS_TYPE_SELECTOR, 
                SelectorsEnum.PAGE_SIZE_SELECTOR, SelectorsEnum.PAGE_SELECTOR,
                SelectorsEnum.PLAYED_ALL_MATCHES_SELECTOR])
    }

    executeDataRequest(dataRequest: DataRequest, 
            callback: (loadingState: LoadingEnum, result?: RestTableData<TeamGoalPoints>) => void): void {
        const leveRequest = this.props.levelDataProps.createLevelRequest()
        getTeamGoalPoints(leveRequest, dataRequest.statisticsParameters, dataRequest.playedAllMatches,
            callback)
    }

    columnHeaders(sortingState: SortingState): JSX.Element {
        return <Translation>
            {
            (t, { i18n }) =>
            <tr>
                <th className="position hint" popped-hint={t('table.position')}>{t('table.position_abbr')}</th>
                <th>{t('table.team')}</th>
                <th className="value">{t('table.league')}</th>
                <ModelTableTh title='table.win_abbr' sortingField='won' poppedHint={t('table.win')} sortingState={sortingState} />
                <ModelTableTh title='table.lose_abbr' sortingField='lost' poppedHint={t('table.lose')} sortingState={sortingState} />
                <ModelTableTh title='table.draw_abbr' sortingField='draw' poppedHint={t('table.draw')} sortingState={sortingState} />
                <ModelTableTh title='table.goals_for_abbr' sortingField='goals_for' poppedHint={t('table.goals_for')} sortingState={sortingState} />
                <ModelTableTh title='table.goals_against_abbr' sortingField='goals_against' poppedHint={t('table.goals_against')} sortingState={sortingState} />
                <ModelTableTh title='table.goals_difference' sortingField='goals_difference' sortingState={sortingState} />
                <ModelTableTh title='table.points_abbr' sortingField='points' poppedHint={t('table.points')} sortingState={sortingState} />
            </tr>
        }
        </Translation>
    }

    columnValues(index: number, teamRating: TeamGoalPoints): JSX.Element {
        let teamSortingKey = teamRating.teamSortingKey
        return <>
            <td>{index + 1}</td>
            <td><TeamLink id={teamSortingKey.teamId} text={teamSortingKey.teamName} /></td>
            <td className="value"><LeagueUnitLink id={teamSortingKey.leagueUnitId} text={teamSortingKey.leagueUnitName}/></td>
            <td className="value">{teamRating.won}</td>
            <td className="value">{teamRating.lost}</td>
            <td className="value">{teamRating.draw}</td>
            <td className="value">{teamRating.goalsFor}</td>
            <td className="value">{teamRating.goalsAgaints}</td>
            <td className="value">{teamRating.goalsDifference}</td>
            <td className="value">{teamRating.points}</td>
        </>
    }
}

export default TeamGoalPointsTable