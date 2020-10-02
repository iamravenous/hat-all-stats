import React from 'react';
import LeagueData from '../rest/models/LeagueData'
import '../common/menu/TopMenu.css'
import TopMenu from '../common/menu/TopMenu'
import { toArabian } from "../common/Utils"

interface Props {
    leagueData?: LeagueData,
    callback: (divisionLevel: number) => void
}

class LeagueTopMenu extends TopMenu<Props> {
  onChanged = (event: React.FormEvent<HTMLSelectElement>) => {
    this.props.callback(toArabian(event.currentTarget.value))
  }

  selectBox(): JSX.Element {
        return  <select className="href_select" onChange={this.onChanged}>
          <option value={undefined}>Select...</option>
          {this.props.leagueData?.divisionLevels.map(divisionLevel => {
            return <option value={divisionLevel} key={'division_level_select_' + divisionLevel}>{divisionLevel}</option>}
          )}
        </select>
  }

  links(): [string, string?][] {
    return [
      ["/league/" + this.props.leagueData?.leagueId, this.props.leagueData?.leagueName]
    ]
  }
}

export default LeagueTopMenu