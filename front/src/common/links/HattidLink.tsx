import React from 'react'
import { PagesEnum } from "../enums/PagesEnum";
import Mappings from '../enums/Mappings'
import { Link } from 'react-router-dom';
import './TableLink.css'

export interface LinkProps {
    text: string | JSX.Element,
    tableLink?: boolean,
    page?: PagesEnum,
    sortingField?: string,
    rowNumber?: number,
    season?: number,
    round?: number,
    forceRefresh?: boolean
}

abstract class HattidLink<Props extends LinkProps> extends React.Component<Props, {}> {
    abstract baseString(): string

    render() {
        let parameters: any = {}
        let page: PagesEnum | undefined = this.props.page
        if(page) {    
            parameters.page = Mappings.queryParamToPageMap.getKey(page as PagesEnum)
        }
        if(this.props.sortingField) {
            parameters.sortingField = this.props.sortingField || ''
        }
        if(this.props.rowNumber !== undefined) {
            parameters.row = this.props.rowNumber
        }
        if(this.props.round !== undefined) {
            parameters.round = this.props.round
        }
        if(this.props.season) {
            parameters.season = this.props.season || 0
        }

        let queryParams = new URLSearchParams(parameters).toString()

        let className: string
        if(this.props.tableLink !== undefined) {
            className = (this.props.tableLink) ? "table_link" : "left_bar_link page"
        } else {
            className = "table_link"
        }

        //**cking workaround. Can't update the page.... 
        //TODO
        let cback: () => void = () => {}
        if(this.props.forceRefresh !== undefined && this.props.forceRefresh) {
            cback = () => {setTimeout( () => {window.location.reload()}, 100)}
        } 

        return <Link className={className} 
            to={this.baseString() + '?' + queryParams}
            onClick={cback} >
                {this.props.text}
            </Link>
    }
}

export default HattidLink