import React from 'react';
import LevelData from '../../../rest/models/leveldata/LevelData';
import { SortingState } from '../AbstractTableSection'
import ClassicTableSection from '../ClassicTableSection'
import LevelDataProps, { LevelDataPropsWrapper } from '../../LevelDataProps'
import '../../../i18n'
import { Translation } from 'react-i18next'
import TeamCards from '../../../rest/models/team/TeamCards';
import ModelTableTh from '../../../common/elements/SortingTableTh'
import LeagueUnitLink from '../../links/LeagueUnitLink';
import TeamLink from '../../links/TeamLink'
import { yellowCards, redCards } from '../../Formatters'
import { StatsTypeEnum } from '../../../rest/models/StatisticsParameters';
import { getTeamCards } from '../../../rest/Client';


abstract class TeamCardsTable<Data extends LevelData, TableProps extends LevelDataProps<Data>>
    extends ClassicTableSection<Data, TableProps, TeamCards> {

    constructor(props: LevelDataPropsWrapper<Data, TableProps>) {
        super(props, 'yellow_cards', {statType: StatsTypeEnum.ROUND, roundNumber: props.levelDataProps.currentRound()},
            [StatsTypeEnum.ROUND])
    }

    fetchDataFunction = getTeamCards

    columnHeaders(sortingState: SortingState): JSX.Element {
        return <Translation>
            {
            (t, { i18n }) =>
            <tr>
                <th className="position hint" popped-hint={t('table.position')}>{t('table.position_abbr')}</th>
                <th>{t('table.team')}</th>
                <th className="value">{t('table.league')}</th>
                <ModelTableTh title='table.yellow_cards' sortingField='yellow_cards' sortingState={sortingState} />
                <ModelTableTh title='table.red_cards' sortingField='red_cards' sortingState={sortingState} />
            </tr>
        }
        </Translation>
    }

    columnValues(index: number, teamCards: TeamCards): JSX.Element {
        let teamSortingKey = teamCards.teamSortingKey
        return <>
            <td>{index + 1}</td>
            <td><TeamLink id={teamSortingKey.teamId} text={teamSortingKey.teamName} /></td>
            <td className="value"><LeagueUnitLink id={teamSortingKey.leagueUnitId} text={teamSortingKey.leagueUnitName}/></td>
            <td className="value">{yellowCards(teamCards.yellowCards)}</td>
            <td className="value">{redCards(teamCards.redCards)}</td>
        </>
    }
}

export default TeamCardsTable