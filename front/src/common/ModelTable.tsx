import React from 'react';
import './ModelTable.css'
import StatisticsParameters, { SortingDirection, StatsTypeEnum, StatsType } from '../rest/StatisticsParameters'
import RestTableData from '../rest/RestTableData'
import PageNavigator from '../common/PageNavigator'
import Cookies from 'js-cookie'
import PageSizeSelector from './selectors/PageSizeSelector'
import StatsTypeSelector from './selectors/StatsTypeSelector'
import SeasonSelector from './selectors/SeasonSelector'
import LevelData from '../rest/models/LevelData';
import StatisticsSection from './StatisticsSection'
import LevelRequest from '../rest/models/request/LevelRequest';

interface ModelTableState<T> {
    entities?: Array<T>,
    isError: boolean, 
    statisticsParameters: StatisticsParameters,
    isLastPage: boolean,
    dataLoading: boolean
}

export interface ModelTablePropsWrapper<Data extends LevelData, TableProps extends ModelTableProps<Data>> {
    modelTableProps: TableProps
}

export abstract class ModelTableProps<Data extends LevelData> {
    levelData: Data

    constructor(levelData: Data) {
        this.levelData = levelData
    }

    abstract leagueId(): number

    currentSeason(): number {
        return this.seasonRoundInfo()[this.seasonRoundInfo().length - 1][0]
    }
    seasons(): Array<number> {
        return this.seasonRoundInfo().map(seasonInfo => seasonInfo[0])
    }
    currentRound(): number {
        let rounds = this.seasonRoundInfo()[this.seasonRoundInfo().length - 1][1]
        return rounds[rounds.length - 1]
    }

    rounds(seas: number): Array<number> {
        let r = this.seasonRoundInfo().filter(season => season[0] === seas )
        return r[0][1]
    }

    seasonRoundInfo(): Array<[number, Array<number>]> {return this.levelData.seasonRoundInfo}

    currency(): string {return this.levelData.currency}

    currencyRate(): number {return this.levelData.currencyRate}

    abstract createLevelRequest(): LevelRequest
}

export interface SortingState {
    callback: (sortingField: string) => void,
    currentSorting: string,
    sortingDirection: SortingDirection
}

abstract class ModelTable<Data extends LevelData, TableProps extends ModelTableProps<Data>, Model> 
        extends StatisticsSection<ModelTablePropsWrapper<Data, TableProps>, ModelTableState<Model>> {
    private statsTypes: Array<StatsTypeEnum>

    constructor(props: ModelTablePropsWrapper<Data, TableProps>, 
            defaultSortingField: string, defaultStatsType: StatsType,
            statsTypes: Array<StatsTypeEnum>) {
        super(props, '')
        this.statsTypes = statsTypes
        
        let pageSizeString = Cookies.get('hattid_page_size')
        let pageSize = (pageSizeString == null) ? 16 : Number(pageSizeString)
        this.state={
            isLastPage: true,
            statisticsParameters: {
                page: 0,
                pageSize: pageSize,
                sortingField: defaultSortingField,
                sortingDirection: SortingDirection.DESC,
                statsType: defaultStatsType,
                season: this.props.modelTableProps.currentSeason()
            },
            dataLoading: false,
            isError: false
        }

        this.pageSizeChanged=this.pageSizeChanged.bind(this);
        this.sortingChanged=this.sortingChanged.bind(this);
        this.statTypeChanged=this.statTypeChanged.bind(this);
        this.seasonChanged=this.seasonChanged.bind(this);
        this.updateCurrent=this.updateCurrent.bind(this);
    }

    fetchEntities(tableProps: TableProps, 
            statisticsParameters: StatisticsParameters, 
            callback: (restTableData: RestTableData<Model>) => void, 
            onError: () => void): void {
        const leveRequest = tableProps.createLevelRequest()
        this.fetchDataFunction(leveRequest, statisticsParameters, callback, onError)
    }

    abstract fetchDataFunction(levelRequest: LevelRequest,
        statisticsParameters: StatisticsParameters,
        callback: (restTableData: RestTableData<Model>) => void,
        onError: () => void): void

    createColumnHeaders(): JSX.Element {
        const sortingState = {
            callback: this.sortingChanged,
            currentSorting: this.state.statisticsParameters.sortingField,
            sortingDirection: this.state.statisticsParameters.sortingDirection
        }
        return this.columnHeaders(sortingState)
    }

    abstract columnHeaders(sortingState: SortingState): JSX.Element

    abstract columnValues(index: number, model: Model): JSX.Element

    componentDidMount() {
        this.updateCurrent()
    }

    updateCurrent(): void {
        this.update(this.state.statisticsParameters)
    }

    update(statisticsParameters: StatisticsParameters) {
        
        this.setState({
            entities: this.state.entities,
            statisticsParameters: this.state.statisticsParameters,
            isLastPage: this.state.isLastPage,
            dataLoading: true,
            isError: false
        })

        this.fetchEntities(this.props.modelTableProps,
            statisticsParameters,
            restTableData => this.setState({
                entities: restTableData.entities,
                statisticsParameters: statisticsParameters,
                isLastPage: restTableData.isLastPage,
                dataLoading: false,
                isError: false
            }), 
            () => {
                this.setState({
                    entities: this.state.entities,
                    statisticsParameters: this.state.statisticsParameters,
                    isLastPage: this.state.isLastPage,
                    dataLoading: false,
                    isError: true
                })
            })
    }

    pageSelected(pageNumber: number) {
        let newStatisticsParameters = Object.assign({}, this.state.statisticsParameters)
        newStatisticsParameters.page = pageNumber

        this.update(newStatisticsParameters)
    }

    pageSizeChanged(pageSize: number) {
        let newStatisticsParameters = Object.assign({}, this.state.statisticsParameters)
        newStatisticsParameters.pageSize = pageSize
        newStatisticsParameters.page = 0

        Cookies.set('hattid_page_size', pageSize.toString(), { sameSite: "Lax" })

        this.update(newStatisticsParameters)
    }

    sortingChanged(sortingField: string) {
        let newStatisticsParameters = Object.assign({}, this.state.statisticsParameters)
        
        let newSortingDirection: SortingDirection
        if( this.state.statisticsParameters.sortingField === sortingField ) {
            if ( this.state.statisticsParameters.sortingDirection === SortingDirection.DESC ) {
                newSortingDirection = SortingDirection.ASC
            } else {
                newSortingDirection = SortingDirection.DESC
            }
        } else {
            newSortingDirection = SortingDirection.DESC
        }

        newStatisticsParameters.sortingField = sortingField
        newStatisticsParameters.sortingDirection = newSortingDirection
        this.update(newStatisticsParameters)
    }

    statTypeChanged(statType: StatsType) {
        let newStatisticsParameters = Object.assign({}, this.state.statisticsParameters)
        newStatisticsParameters.statsType = statType

        this.update(newStatisticsParameters)
    }

    seasonChanged(season: number) {
        let newStatisticsParameters = Object.assign({}, this.state.statisticsParameters)
        newStatisticsParameters.season = season

        this.update(newStatisticsParameters)
    }

    renderSection(): JSX.Element {
        let navigatorProps = {
            pageNumber: this.state.statisticsParameters.page,
            isLastPage: this.state.isLastPage
        }

        return <>
                <div className="table_settings_div">
                    <SeasonSelector currentSeason={this.props.modelTableProps.currentSeason()}
                        seasons={this.props.modelTableProps.seasons()}
                        callback={this.seasonChanged}/>
                    <StatsTypeSelector  statsTypes={this.statsTypes}
                        rounds={this.props.modelTableProps.rounds(this.state.statisticsParameters.season)}
                        selectedStatType={this.state.statisticsParameters.statsType}
                        onChanged={this.statTypeChanged}
                        />

                    <PageSizeSelector 
                        selectedSize={this.state.statisticsParameters.pageSize}
                        linkAction={this.pageSizeChanged}/>
                </div>
                <table className="statistics_table">
                    <thead>
                        {this.createColumnHeaders()}
                    </thead>
                    <tbody>
                        {this.state.entities?.map((entity, index) => 
                            this.columnValues(this.state.statisticsParameters.pageSize * this.state.statisticsParameters.page + index, entity))}
                    </tbody>
                </table>

                <PageNavigator 
                    pageNumber={navigatorProps.pageNumber} 
                    isLastPage={navigatorProps.isLastPage} 
                    linkAction={(pageNumber) => this.pageSelected(pageNumber)}/> 
                </>
    }
}

export default ModelTable;