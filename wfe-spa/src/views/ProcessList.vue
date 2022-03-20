<template>
    <v-container
        id="processes-view"
        fluid
        tag="section"
    >
        <v-data-table
            class="elevation-1 wfe-process-table"
            :item-class="getClass"
            :headers="headers"
            :items="processes"
            item-key="id"
            :options.sync="options"
            :server-items-length="total"
            :loading="loading"
            :footer-props="{
                disablePagination: false,
                disableItemsPerPage: false,
                itemsPerPageAllText: 'Все',
                itemsPerPageText: 'Строк на странице',
            }"
            >
            <template v-for="header in headers" v-slot:[`item.${header.value}`]="{ item }">
                <div v-if="header.dynamic">
                    {{ getVariableValue(header.text, item) }}
                </div>
                <div v-else-if="header.value==='startDate' || header.value==='endDate'">
                    {{ getDateTime(item[header.value]) }}
                </div>
                <div v-else-if="header.value==='definitionName'">
                    <card-link :routeName="`Карточка процесса`" :id="item.id" :text="item.definitionName" />
                </div>
                <div v-else>
                    {{ item[header.value] }}
                </div>
            </template>
            <template v-slot:[`footer.page-text`]="items">
                {{ items.pageStart }} - {{ items.pageStop }} из {{ items.itemsLength }}
            </template>
            <template v-slot:no-data>
                Данные отсутствуют
            </template>
            <template v-slot:[`body.prepend`]>
                <tr v-if="filterVisible" class="filter-row">
                    <filter-cell v-for="header in headers"
                        :header="header"
                        :key="header.value"
                        v-model="filter[header.value]"
                        @update-filter-event="checkFilter()"
                    />
                </tr>
            </template>
            <template v-slot:top>
                <v-toolbar flat>
                    <v-spacer/>
                    <v-btn 
                        text 
                        icon
                        color="rgba(0, 0, 0, 0.67)"
                        @click="reloadBtnIcon = 'mdi-reload'; getDataFromApi()"
                    >
                        <v-icon>{{reloadBtnIcon}}</v-icon>
                    </v-btn>
                    <v-btn 
                        text 
                        icon
                        @click="toggleFilterVisible()"
                        v-model="filterVisible" 
                        color="rgba(0, 0, 0, 0.67)"
                    >
                        <v-icon>mdi-filter</v-icon>
                    </v-btn>
                    <v-btn 
                        text 
                        icon
                        :depressed="!filterNow"
                        :disabled="!filterNow"
                        color="rgba(0, 0, 0, 0.67)"
                        @click="clearFilters(); getDataFromApi()"
                    >
                        <v-icon>{{cancelBtnIcon}}</v-icon>
                    </v-btn>
                    <columns-visibility :initialHeaders="initialHeaders" :variables="variables" :filter="filter" @update-data-event="updateData"/>
                    <color-description :colors="colors" />
                </v-toolbar>
            </template>
        </v-data-table>
    </v-container>
</template>

<script lang="ts">
import Vue from 'vue';
import { get, sync } from 'vuex-pathify';
import { Options, Sorting } from '../ts/Options';

export default Vue.extend({
    name: "ProcessList",

    data() {
        return {
            dialog: false,
            filterVisible: false,
            filter: {
                id: null,
                definitionName: null,
                executionStatus: null,
                startDate: null,
                endDate: null
            },
            variables: [],
            total: 0,
            processes: [],
            loading: true,
            options: new Options(),
            reloadBtnIcon: 'mdi-reload',
            cancelBtnIcon: 'mdi-close',
            filterNow: false,
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
                    format: 'Long',
                },
                { 
                    text: 'Процесс', 
                    value: 'definitionName',
                    visible: true,
                    width: '20em',
                    format: 'String',
                },
                { 
                    text: 'Статус', 
                    value: 'executionStatus',
                    visible: true,
                    width: '20em',
                    format: 'String',
                },
                { 
                    text: 'Запущен', 
                    value: 'startDate',
                    visible: true,
                    sortable: false,
                    width: '10em',
                    format: 'DateTime',
                },
                { 
                    text: 'Окончен', 
                    value: 'endDate',
                    visible: true,
                    width: '10em',
                    format: 'DateTime',
                },
            ]
        }
    },
    mounted() {
        if (localStorage.getItem('runawfe@process-list-variables')) {
            try {
                this.variables = JSON.parse(localStorage.getItem('runawfe@process-list-variables'));
            } catch(e) {
                localStorage.removeItem('runawfe@process-list-variables');
            }
        }
        if (localStorage.getItem('runawfe@process-list-initialHeaders')) {
            try {
                this.initialHeaders = JSON.parse(localStorage.getItem('runawfe@process-list-initialHeaders'));
            } catch(e) {
                localStorage.removeItem('runawfe@process-list-initialHeaders');
            }
        }
    },
    computed: {
        headers(): any {
            return this.initialHeaders.filter((h: any) => {
                return h.visible;
            });
        },
    },
    watch: {
        options: {
            handler () {
                this.getDataFromApi()
            },
            deep: true,
        },
        filter: {
            handler () {
                //this.getDataFromApi()
            },
            deep: true,
        }
    },
    methods: {
        toggleFilterVisible () {
            if (!this.filterNow) {
                this.filterVisible = !this.filterVisible;
            }
        },
        isAnyFilter () {
            for (let prop in this.filter) { 
                if (this.filter.hasOwnProperty(prop)) {
                    if (this.filter[prop] !== null && this.filter[prop] !== '') {
                        return true;
                    }
                }
            }
            return false;
        },
        checkFilter () {
            if (!this.isAnyFilter()) {
                this.filterNow = false;
                this.reloadBtnIcon = 'mdi-reload';
                this.getDataFromApi();
            } else {
                this.reloadBtnIcon = 'mdi-check-all';
                this.filterNow = true;
            }
        },
        clearFilters () {
            this.filterNow = false;
            this.cancelBtnIcon = 'mdi-close';
            this.reloadBtnIcon = 'mdi-reload';
            for (let prop in this.filter) {
                if (this.filter.hasOwnProperty(prop)) {
                    this.filter[prop] = null;
                }
            }
            this.filterVisible = false;
        },
        updateData () {
            this.getDataFromApi();
            localStorage.setItem('runawfe@process-list-variables', JSON.stringify(this.variables));
            localStorage.setItem('runawfe@process-list-initialHeaders', JSON.stringify(this.initialHeaders));
        },
        getDateTime (value: string) {
            // TODO date.format.pattern, default dd.MM.yyyy HH:mm
            if (!value) return '';
            return new Date(value).toLocaleString("ru", {day: "numeric", month: "numeric", year: "numeric", hour: "numeric", minute: "numeric"}).replace(',','');
        },
        getDate (value: string) {
            // TODO date.format.pattern, default dd.MM.yyyy
            if (!value) return '';
            return new Date(value).toLocaleDateString("ru", {day: "numeric", month: "numeric", year: "numeric"});
        },
        getTime (value: string) {
            if (!value) return '';
            // Time format is always HH:mm
            return new Date(value).toLocaleTimeString("ru", {hour: "numeric", minute: "numeric"});
        },
        getVariableValue (variableName, data) {
            for (let variable of data.variables) {
                if (variableName == variable.name ) {
                    const prefix = 'ru.runa.wfe.var.format.';
                    if (!variable.format.includes(prefix)) {
                        let obj = {};
                        obj = Object.assign(obj, variable.value);
                        if (obj && Object.keys(obj).length !== 0) {
                            return variable.value;
                        }
                        return '';
                    }
                    const format = variable.format.replace(prefix,'').replace('Format','');
                    if (format === 'Date') {
                        return this.getDate(variable.value);
                    } else if (format === 'Time') {
                        return this.getTime(variable.value);
                    } else if (format === 'DateTime') {
                        return this.getDateTime(variable.value);
                    } else if (format === 'Actor' || format === 'Executor' || format === 'Group') {
                        let executor = {};
                        executor = Object.assign(executor, variable.value);
                        return executor.name;
                    } else if (format === 'File') {
                        // not support
                        return '';
                    } else if (format.includes('List')) {
                        let arr = [];
                        arr = Object.assign(arr, variable.value); 
                        if (arr.length) {
                            return variable.value;
                        }
                        return '';
                    } else if (format.includes('Map')) {
                        let obj = {};
                        obj = Object.assign(obj, variable.value); 
                        if (obj && Object.keys(obj).length !== 0) {
                            return variable.value;
                        }
                        return '';
                    } else {
                        return variable.value;
                    }
                }
            }
        },
        getClass (process: any) {
            let cl = '';
            const timestamp = new Date().getTime();
            if (process.endDate != null && process.endDate > timestamp) {
                cl = 'process2';
            }
            return cl;
        },
        getDataFromApi () {
            this.loading = true;
            const { page, itemsPerPage, sortBy, sortDesc } = this.options;
            const query = {
                filters: this.filter,
                pageNumber: page,
                pageSize: itemsPerPage,
                sortings: Sorting.convert(sortBy, sortDesc),
                variables: this.variables
            };
            this.$apiClient().then((client: any) => {
                client['process-api-controller'].getProcessesUsingPOST(null, { requestBody: query }).then((data: any) => {
                    const body = data.body;
                    if (body) {
                        this.processes = body.data;
                        this.total = body.total;
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