<template>
    <v-container
        id="processes-definition-view"
        fluid
        tag="section"
    >
        <wfe-tables
            :initialHeaders="initialHeaders"
            :records="definitions"
            :loading="loading"
            :routeName="`ProcessDefinitionCard`"
            :prefixLocalStorageName="`runawfe@definition-list`"
            :dynamic="false"
            :options="options"
            @get-data-event="onGetData"
            :footerProps= "{
                disablePagination: false,
                disableItemsPerPage: false,
                itemsPerPageAllText: 'Все',
                itemsPerPageText: 'Строк на странице',
                itemsPerPageOptions: [10, 20, 100, 500]
            }"
        />
    </v-container>
</template>

<script lang="ts">
import Vue from 'vue';
import { sync } from 'vuex-pathify';
import { Sorting } from '../ts/Options';
import Constants from '../ts/Constants';

export default Vue.extend({
    name: "ProcessDefinitionList",

    data() {
        return {
            total: 0,
            definitions: [],
            loading: true,
            initialHeaders: [
                {
                    text: 'Запустить',
                    value: 'start',
                    align: 'center',
                    visible: true,
                    sortable: false,
                    width: '1px',
                    bcolor: Constants.WHITE_COLOR,
                    filterable: false,
                },
                {
                    text: 'Имя',
                    align: 'start',
                    value: 'name',
                    visible: true,
                    width: '20em',
                    bcolor: Constants.WHITE_COLOR,
                    format: 'String',
                    filterable: true,
                },
                {
                    text: 'Описание',
                    value:'description',
                    visible: false,
                    width: '20em',
                    bcolor: Constants.WHITE_COLOR,
                    format: 'String',
                    filterable: true,
                },
                {
                    text: 'Дата загрузки',
                    value: 'createDate',
                    visible: true,
                    width: '12em',
                    bcolor: Constants.WHITE_COLOR,
                    format: 'DateTime',
                    filterable: true,
                },
                {
                    text: 'Автор загрузки',
                    value: 'createActor',
                    visible: true,
                    sortable: false,
                    width: '12em',
                    bcolor: Constants.WHITE_COLOR,
                    format: 'Actor',
                    filterable: true,
                },
                {
                    text: 'Дата обновления',
                    value: 'updateDate',
                    visible: false,
                    width: '12em',
                    bcolor: Constants.WHITE_COLOR,
                    format: 'DateTime',
                    filterable: true,
                },
                {
                    text: 'Автор обновления',
                    value: 'updateActor',
                    visible: false,
                    sortable: false,
                    width: '12em',
                    bcolor: Constants.WHITE_COLOR,
                    format: 'Actor',
                    filterable: true,
                }
            ]
        }
    },
    computed: {
        items: sync('app/items'),
        options(): any {
            return this.items.find(h => h.to === Constants.DEFINITIONS_PATH).options;
        }
    },
    watch: {
    },
    methods: {
        getFilters (filter) {
            let result = Object.assign({}, filter);
            for (let prop in filter) {
                if (filter.hasOwnProperty(prop)) {
                    if (filter[prop] !== null && filter[prop] !== '') {
                        let header = this.initialHeaders.find(h => h.value === prop);
                        if(!header || (header.format !== 'DateTime' && header.format !== 'Long' && !header.selectOptions)) {
                            result[prop] = '*' + filter[prop].trim() + '*/i';
                        }
                    }
                }
            }
            return result;
        },
        onGetData (options, filter) {
            this.items.find(h => h.to === Constants.DEFINITIONS_PATH).options = options;
            localStorage.setItem(Constants.DEFINITIONS_OPTIONS, JSON.stringify(options));
            this.loading = true;
            const { page, itemsPerPage, sortBy, sortDesc } = options;
            const query = {
                filters: this.getFilters(filter),
                pageNumber: page,
                pageSize: itemsPerPage,
                sortings: Sorting.convert(sortBy, sortDesc),
            };
            this.$apiClient().then((client: any) => {
                client['definition-controller'].getProcessDefinitionsUsingPOST(null, { requestBody: query }).then((data: any) => {
                    const body = data.body;
                    if (body) {
                        this.definitions = body.data;
                        this.total = body.total;
                    }
                    this.loading = false;
                }).catch((error: any) => {
                    this.loading = false;
                    this.definitions = [];
                    this.total = 0;
                });
            });
        },
    },
});
</script>
