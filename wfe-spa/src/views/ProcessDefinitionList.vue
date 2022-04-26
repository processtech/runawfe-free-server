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
            @get-data-event="onGetData"
        />
    </v-container>
</template>

<script lang="ts">
import Vue from 'vue';
import { get, sync } from 'vuex-pathify';
import { Options, Sorting } from '../ts/Options';

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
                    filterable: false,
                },
                {
                    text: 'Имя',
                    align: 'start',
                    value: 'name',
                    visible: true,
                    width: '20em',
                    bcolor: '',
                    format: 'String',
                    filterable: true,
                },
                {
                    text: 'Описание',
                    value:'description',
                    visible: false,
                    width: '20em',
                    bcolor: '',
                    format: 'String',
                    filterable: true,
                },
                {
                    text: 'Дата загрузки',
                    value: 'createDate',
                    visible: true,
                    width: '12em',
                    bcolor: '',
                    format: 'DateTime',
                    filterable: true,
                },
                {
                    text: 'Автор загрузки',
                    value: 'createActor',
                    visible: true,
                    sortable: false,
                    width: '12em',
                    bcolor: '',
                    format: 'Actor',
                    filterable: true,
                },
                {
                    text: 'Дата обновления',
                    value: 'updateDate',
                    visible: false,
                    width: '12em',
                    bcolor: '',
                    format: 'DateTime',
                    filterable: true,
                },
                {
                    text: 'Автор обновления',
                    value: 'updateActor',
                    visible: false,
                    sortable: false,
                    width: '12em',
                    bcolor: '',
                    format: 'Actor',
                    filterable: true,
                }
            ]
        }
    },
    computed: {
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
            this.loading = true;
            const { page, itemsPerPage, sortBy, sortDesc } = options;
            const query = {
                filters: this.getFilters(filter),
                pageNumber: page,
                pageSize: itemsPerPage,
                sortings: Sorting.convert(sortBy, sortDesc),
            };
            this.$apiClient().then((client: any) => {
                client['process-definition-api-controller'].getDefinitionsUsingPOST(null, { requestBody: query }).then((data: any) => {
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