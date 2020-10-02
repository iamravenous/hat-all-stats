import React from 'react'
import LeagueUnitData from '../rest/models/LeagueUnitData'
import '../common/menu/TopMenu.css'
import TopMenu from '../common/menu/TopMenu';

interface Props {
    leagueUnitData?: LeagueUnitData,
    callback: (teamId: number) => void
}

class LeagueUnitTopMenu extends TopMenu<Props> {

    onChanged = (event: React.FormEvent<HTMLSelectElement>) => {
        this.props.callback(Number(event.currentTarget.value))
    }

    links(): [string, string?][] {
        return [
            ["/league/" + this.props.leagueUnitData?.leagueId, this.props.leagueUnitData?.leagueName],
            ["/league/" + this.props.leagueUnitData?.leagueId + "/divisionLevel/" + this.props.leagueUnitData?.divisionLevel, this.props.leagueUnitData?.divisionLevelName],
            ["/leagueUnit/" + this.props.leagueUnitData?.leagueUnitId, this.props.leagueUnitData?.leagueUnitName]
        ]
    }

    selectBox(): JSX.Element {
        return <select className="href_select" onChange={this.onChanged}>
                <option value={undefined}>Select...</option>
                {this.props.leagueUnitData?.teams.map(([teamId, teamName]) => {
                    return <option value={teamId}>{teamName}</option>
                })}
            </select>
    }
}

export default LeagueUnitTopMenu