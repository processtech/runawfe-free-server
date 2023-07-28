<template>
    <v-container
        id="processes-view"
        fluid
        tag="section"
    >
        <wfe-tables
            :initialHeaders="initialHeaders"
            :records="processes"
            :colors="colors"
            :total="total"
            :loading="loading"
            :routeName="`Карточка процесса`"
            :prefixLocalStorageName="`runawfe@process-list`"
            :dynamic="true"
            @get-data-event="onGetData"
        />
    </v-container>
</template>

<script lang="ts">
import Vue from 'vue';
import { get, sync } from 'vuex-pathify';
import { Options, Sorting, Select, Header } from '../ts/Options';

export default Vue.extend({
    name: "ProcessList",

    data() {
        return {
            total: 0,
            processes: [],
            loading: true,
            colors: [
                {
                    value: 'process1',
                    desc: 'Установленный срок окончания процесса подходит к концу'
                },
                {
                    value: 'process2',
                    desc: 'Процесс не завершён в установленный срок'
                },
            ],
            initialHeaders: [
                {
                    text: '№ экз.',
                    align: 'start',
                    value: 'id',
                    visible: true,
                    width: '3em',
                    bcolor: '',
                    format: 'Long',
                    filterable: true,
                },
                {
                    text: 'Процесс',
                    value: 'definitionName',
                    visible: true,
                    width: '20em',
                    bcolor: '',
                    format: 'String',
                    link: true,
                    filterable: true,
                },
                {
                    text: 'Статус',
                    value: 'executionStatus',
                    visible: true,
                    width: '20em',
                    bcolor: '',
                    format: 'String',
                    selectOptions:[new Select('Активен','ACTIVE'), new Select('Завершен','ENDED'),
                                   new Select('Приостановлен','SUSPENDED'), new Select('Имеет ошибки выполнения','FAILED')],
                    filterable: true,
                },
                {
                    text: 'Запущен',
                    value: 'startDate',
                    visible: true,
                    sortable: false,
                    width: '10em',
                    bcolor: '',
                    format: 'DateTime',
                    filterable: true,
                },
                {
                    text: 'Окончен',
                    value: 'endDate',
                    visible: true,
                    width: '10em',
                    bcolor: '',
                    format: 'DateTime',
                    filterable: true,
                },
            ]
        }
    },
    mounted() {
    },
    computed: {
    },
    watch: {
    },
    methods: {
        getClass (process: any) {
            let cl = '';
            const timestamp = new Date().getTime();
            if (process.endDate != null && process.endDate > timestamp) {
                cl = 'process2';
            }
            return cl;
        },
        getClasses (processes: any) {
            processes.forEach(process => {
                process.class = this.getClass(process);
            });
        },
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
        onGetData (options, filter, variables) {
            this.loading = true;
            const { page, itemsPerPage, sortBy, sortDesc } = options;
            const query = {
                filters: this.getFilters(filter),
                pageNumber: page,
                pageSize: itemsPerPage,
                sortings: Sorting.convert(sortBy, sortDesc),
                variables: variables
            };
            this.$apiClient().then((client: any) => {
                client['process-controller'].getProcessesUsingPOST(null, { requestBody: query }).then((data: any) => {
                    const body = data.body;
                    if (body) {
                        this.processes = body.data;
                        this.total = body.total;
                        this.getClasses(this.processes);
                    }
                    this.loading = false;
                }).catch((error: any) => {
                    this.loading = false;
                    this.processes = [];
                    this.total = 0;
                });
            });
        },
    },
});
</script>